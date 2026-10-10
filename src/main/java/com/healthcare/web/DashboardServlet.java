package com.healthcare.web;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.healthcare.data.AppointmentRepository;
import com.healthcare.data.AvailabilityRepository;
import com.healthcare.data.FeedbackRepository;
import com.healthcare.data.MedicalRecordRepository;
import com.healthcare.data.PatientHistoryRepository;
import com.healthcare.data.SettingRepository;
import com.healthcare.data.UserRepository;
import com.healthcare.model.Appointment;
import com.healthcare.model.AppointmentDay;
import com.healthcare.model.AnalyticsPoint;
import com.healthcare.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/dashboard", "/appointments", "/records", "/admin/users", "/admin/settings",
        "/doctor/records", "/doctor/schedule", "/patient/profile", "/patient/feedback"})
public class DashboardServlet extends HttpServlet {
    private static final DateTimeFormatter APPOINTMENT_INPUT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final int SCHEDULE_DAYS = 7;

    private final UserRepository users = new UserRepository();
    private final AppointmentRepository appointments = new AppointmentRepository();
    private final MedicalRecordRepository records = new MedicalRecordRepository();
    private final PatientHistoryRepository patientHistory = new PatientHistoryRepository();
    private final FeedbackRepository feedback = new FeedbackRepository();
    private final AvailabilityRepository availability = new AvailabilityRepository();
    private final SettingRepository settings = new SettingRepository();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        if (path.equals("/")) {
            User current = currentUser(request);
            response.sendRedirect(request.getContextPath() + (current == null ? "/login" : "/dashboard"));
            return;
        }
        User current = currentUser(request);
        try {
            switch (path) {
                case "/dashboard" -> {
                    request.setAttribute("appointments", appointments.listFor(current));
                    if (current.role().equals("ADMIN")) {
                        List<User> accounts = users.listAll();
                        request.setAttribute("users", accounts);
                        request.setAttribute("accountCount", accounts.size());
                        request.setAttribute("activeDoctorCount", accounts.stream()
                                .filter(account -> account.role().equals("DOCTOR") && account.active())
                                .count());
                        request.setAttribute("activePatientCount", accounts.stream()
                                .filter(account -> account.role().equals("PATIENT") && account.active())
                                .count());
                        request.setAttribute("analytics", appointmentAnalytics(appointments.listFor(current)));
                        request.setAttribute("monthlyAppointments", appointments.monthlyAnalytics());
                        request.setAttribute("monthlyAccounts", users.monthlyRegistrationAnalytics());
                    }
                    if (current.role().equals("DOCTOR")) {
                        request.setAttribute("records", records.listFor(current));
                        long feedbackCount = feedback.countFor(current.id());
                        request.setAttribute("feedbackCount", feedbackCount);
                        request.setAttribute("averageRating", feedbackCount == 0 ? "--"
                                : String.format(Locale.ROOT, "%.1f", feedback.averageFor(current.id())));
                        request.setAttribute("feedbackItems", feedback.listForDoctor(current.id()));
                    }
                    if (current.role().equals("PATIENT")) {
                        request.setAttribute("records", records.listFor(current));
                        request.setAttribute("doctors", users.listDoctors());
                        request.setAttribute("patientHistory", patientHistory.listFor(current));
                    }
                }
                case "/appointments" -> {
                    request.setAttribute("appointments", appointments.listFor(current));
                    request.setAttribute("minimumAppointmentDateTime", LocalDateTime.now().plusMinutes(1)
                            .withSecond(0).withNano(0).format(APPOINTMENT_INPUT_FORMAT));
                    if (current.role().equals("PATIENT")) {
                        request.setAttribute("doctors", users.listDoctors());
                        request.setAttribute("availability", availability.listUpcoming());
                    } else if (current.role().equals("ADMIN")) {
                        request.setAttribute("doctors", users.listDoctors());
                        request.setAttribute("patients", users.listPatients());
                        request.setAttribute("availability", availability.listUpcoming());
                    }
                }
                case "/records" -> {
                    requireOneOf(current, "PATIENT", "DOCTOR");
                    request.setAttribute("records", records.listFor(current));
                    if (current.role().equals("PATIENT")) {
                        request.setAttribute("patientHistory", patientHistory.listFor(current));
                    }
                }
                case "/admin/users" -> {
                    requireRole(current, "ADMIN");
                    request.setAttribute("users", users.listAll());
                }
                case "/admin/settings" -> {
                    requireRole(current, "ADMIN");
                    request.setAttribute("settings", settings.getAll());
                }
                case "/doctor/records" -> {
                    requireRole(current, "DOCTOR");
                    request.setAttribute("appointments", appointments.listFor(current));
                }
                case "/doctor/schedule" -> {
                    requireRole(current, "DOCTOR");
                    request.setAttribute("availability", availability.listFor(current));
                    request.setAttribute("minimumAvailabilityDate", LocalDate.now());
                    List<Appointment> upcoming = appointments.listFor(current).stream()
                            .filter(appointment -> appointment.appointmentAt().isAfter(LocalDateTime.now())
                                    && (appointment.status().equals("PENDING")
                                            || appointment.status().equals("CONFIRMED")))
                            .toList();
                    LocalDate today = LocalDate.now();
                    List<AppointmentDay> appointmentDays = new ArrayList<>();
                    for (int offset = 0; offset < SCHEDULE_DAYS; offset++) {
                        LocalDate date = today.plusDays(offset);
                        List<Appointment> dayAppointments = upcoming.stream()
                                .filter(appointment -> appointment.appointmentAt().toLocalDate().equals(date))
                                .sorted(Comparator.comparing(Appointment::appointmentAt))
                                .toList();
                        appointmentDays.add(new AppointmentDay(date, dayAppointments));
                    }
                    request.setAttribute("appointmentDays", appointmentDays);
                }
                case "/patient/profile" -> {
                    requireRole(current, "PATIENT");
                    request.setAttribute("profile", current);
                }
                case "/patient/feedback" -> {
                    requireRole(current, "PATIENT");
                    request.setAttribute("doctors", users.listDoctors());
                }
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
            if (response.isCommitted()) {
                return;
            }
            String title = switch (path) {
                case "/dashboard" -> switch (current.role()) {
                    case "ADMIN" -> "Clinic overview";
                    case "DOCTOR" -> "Doctor dashboard";
                    default -> "Patient dashboard";
                };
                case "/appointments" -> "Appointments";
                case "/records" -> "Medical records";
                case "/admin/users" -> "People";
                case "/admin/settings" -> "Clinic settings";
                case "/doctor/records" -> "Add a visit note";
                case "/doctor/schedule" -> "Schedule availability";
                case "/patient/profile" -> "Your profile";
                case "/patient/feedback" -> "Share feedback";
                default -> "Healthcare portal";
            };
            request.setAttribute("pageTitle", title);
            request.setAttribute("activePath", path);
            Object flash = request.getSession().getAttribute("flash");
            if (flash != null) {
                request.setAttribute("flash", flash);
                request.getSession().removeAttribute("flash");
            }
            String view = switch (path) {
                case "/appointments" -> "appointments";
                case "/records" -> "records";
                case "/admin/users" -> "users";
                case "/admin/settings" -> "settings";
                case "/doctor/records" -> "doctor-records";
                case "/doctor/schedule" -> "schedule";
                case "/patient/profile" -> "profile";
                case "/patient/feedback" -> "feedback";
                case "/dashboard" -> switch (current.role()) {
                    case "ADMIN" -> "admin-dashboard";
                    case "DOCTOR" -> "doctor-dashboard";
                    default -> "patient-dashboard";
                };
                default -> "dashboard";
            };
            request.getRequestDispatcher("/WEB-INF/views/" + view + ".jsp").forward(request, response);
        } catch (SQLException exception) {
            getServletContext().log("Could not load " + path, exception);
            request.setAttribute("pageTitle", "Database connection");
            request.setAttribute("activePath", path);
            request.setAttribute("error", "The database is not available. Start MySQL and check DB_HOST, DB_PORT, DB_NAME, DB_USER, and DB_PASSWORD.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    private User currentUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute("user");
    }

    private void requireRole(User user, String role) {
        if (!user.role().equals(role)) {
            throw new SecurityException("Role not permitted.");
        }
    }

    private void requireOneOf(User user, String... roles) {
        for (String role : roles) {
            if (user.role().equals(role)) {
                return;
            }
        }
        throw new SecurityException("Role not permitted.");
    }

    private List<AnalyticsPoint> appointmentAnalytics(List<com.healthcare.model.Appointment> appointments) {
        String[] statuses = {"PENDING", "CONFIRMED", "COMPLETED", "CANCELLED"};
        List<Long> counts = new ArrayList<>();
        long maximum = 0;
        for (String status : statuses) {
            long count = appointments.stream().filter(appointment -> appointment.status().equals(status)).count();
            counts.add(count);
            maximum = Math.max(maximum, count);
        }
        List<AnalyticsPoint> points = new ArrayList<>();
        for (int index = 0; index < statuses.length; index++) {
            long count = counts.get(index);
            int percentage = maximum == 0 ? 0 : (int) Math.round(count * 100.0 / maximum);
            points.add(new AnalyticsPoint(statuses[index], count, percentage));
        }
        return points;
    }
}