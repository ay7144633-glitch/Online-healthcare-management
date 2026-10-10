package com.healthcare.web;

import java.io.IOException;
import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.healthcare.data.AppointmentRepository;
import com.healthcare.data.AvailabilityRepository;
import com.healthcare.data.FeedbackRepository;
import com.healthcare.data.MedicalRecordRepository;
import com.healthcare.data.PatientHistoryRepository;
import com.healthcare.data.SettingRepository;
import com.healthcare.data.UserRepository;
import com.healthcare.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/actions/*")
public class ActionServlet extends HttpServlet {
    private final UserRepository users = new UserRepository();
    private final AppointmentRepository appointments = new AppointmentRepository();
    private final AvailabilityRepository availability = new AvailabilityRepository();
    private final MedicalRecordRepository records = new MedicalRecordRepository();
    private final PatientHistoryRepository patientHistory = new PatientHistoryRepository();
    private final FeedbackRepository feedback = new FeedbackRepository();
    private final SettingRepository settings = new SettingRepository();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User user = (User) request.getSession().getAttribute("user");
        String action = request.getPathInfo();
        String destination = safeDestination(request, "/dashboard");
        try {
            switch (action == null ? "" : action) {
                case "/appointments/book" -> {
                    requireRole(user, "PATIENT");
                    appointments.book(user.id(), number(request, "doctorId"),
                            appointmentDateTime(request), value(request, "reason"), settings.appointmentDuration());
                    message(request, "Appointment request sent to the doctor.");
                    destination = safeDestination(request, "/appointments");
                }
                case "/appointments/book-for-patient" -> {
                    requireRole(user, "ADMIN");
                    appointments.book(number(request, "patientId"), number(request, "doctorId"),
                            appointmentDateTime(request), value(request, "reason"),
                            settings.appointmentDuration());
                    message(request, "Appointment scheduled for the selected patient.");
                    destination = safeDestination(request, "/appointments");
                }
                case "/appointments/status" -> {
                    requireOneOf(user, "DOCTOR", "ADMIN", "PATIENT");
                    appointments.updateStatus(user, number(request, "appointmentId"), value(request, "status"));
                    message(request, "Appointment updated.");
                    destination = safeDestination(request, "/appointments");
                }
                case "/appointments/reschedule" -> {
                    requireOneOf(user, "DOCTOR", "ADMIN", "PATIENT");
                    appointments.reschedule(user, number(request, "appointmentId"),
                            LocalDateTime.parse(value(request, "appointmentAt")));
                    message(request, "Appointment rescheduled.");
                    destination = safeDestination(request, "/appointments");
                }
                case "/users/create-account" -> {
                    requireRole(user, "ADMIN");
                    String name = value(request, "fullName");
                    String email = value(request, "email");
                    String password = value(request, "password");
                    String role = value(request, "role");
                    if (name.length() < 2 || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                            || password.length() < 8 || !java.util.List.of("ADMIN", "DOCTOR", "PATIENT").contains(role)) {
                        throw new IllegalArgumentException("Enter a name, valid email, password with at least 8 characters, and a valid role.");
                    }
                    users.createUser(name, email, password, role, value(request, "specialty"));
                    message(request, "Account created.");
                    destination = safeDestination(request, "/admin/users");
                }
                case "/users/toggle" -> {
                    requireRole(user, "ADMIN");
                    users.setActive(number(request, "userId"), value(request, "active").equals("true"));
                    message(request, "Account status updated.");
                    destination = safeDestination(request, "/admin/users");
                }
                case "/users/update" -> {
                    requireRole(user, "ADMIN");
                    long accountId = number(request, "userId");
                    String name = value(request, "fullName");
                    String email = value(request, "email");
                    String role = value(request, "role");
                    if (accountId == user.id()) {
                        throw new IllegalArgumentException("Use another administrator account to edit this account.");
                    }
                    if (name.length() < 2 || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                        throw new IllegalArgumentException("Enter a name and valid email address.");
                    }
                    users.updateAccount(accountId, name, email, role, value(request, "specialty"));
                    message(request, "Account details updated.");
                    destination = safeDestination(request, "/admin/users");
                }
                case "/users/delete" -> {
                    requireRole(user, "ADMIN");
                    long accountId = number(request, "userId");
                    if (accountId == user.id()) {
                        throw new IllegalArgumentException("You cannot delete your own administrator account.");
                    }
                    users.deleteAccount(accountId);
                    message(request, "Account deleted.");
                    destination = safeDestination(request, "/admin/users");
                }
                case "/records/create" -> {
                    requireRole(user, "DOCTOR");
                    records.add(user, number(request, "appointmentId"),
                            value(request, "diagnosis"), value(request, "notes"));
                    message(request, "Visit note saved to the patient's record.");
                    destination = safeDestination(request, "/doctor/records");
                }
                case "/records/update" -> {
                    requireRole(user, "DOCTOR");
                    String diagnosis = value(request, "diagnosis");
                    String notes = value(request, "notes");
                    if (diagnosis.isBlank() || notes.isBlank()) {
                        throw new IllegalArgumentException("Diagnosis and visit notes are required.");
                    }
                    records.update(user, number(request, "recordId"), diagnosis, notes);
                    message(request, "Medical record updated.");
                    destination = safeDestination(request, "/records");
                }
                case "/patient-history/create" -> {
                    requireRole(user, "PATIENT");
                    patientHistory.add(user, value(request, "title"), value(request, "details"));
                    message(request, "Personal health history entry added.");
                    destination = safeDestination(request, "/records");
                }
                case "/patient-history/update" -> {
                    requireRole(user, "PATIENT");
                    patientHistory.update(user, number(request, "entryId"),
                            value(request, "title"), value(request, "details"));
                    message(request, "Personal health history entry updated.");
                    destination = safeDestination(request, "/records");
                }
                case "/patient-history/delete" -> {
                    requireRole(user, "PATIENT");
                    patientHistory.delete(user, number(request, "entryId"));
                    message(request, "Personal health history entry removed.");
                    destination = safeDestination(request, "/records");
                }
                case "/availability/save" -> {
                    requireRole(user, "DOCTOR");
                    try {
                        LocalDate date = LocalDate.parse(value(request, "availableDate"));
                        LocalDateTime availableFrom = LocalDateTime.of(date,
                                LocalTime.parse(value(request, "availableStart")));
                        LocalDateTime availableTo = LocalDateTime.of(date,
                                LocalTime.parse(value(request, "availableEnd")));
                        availability.add(user, availableFrom, availableTo);
                    } catch (DateTimeException exception) {
                        throw new IllegalArgumentException("Choose a valid date and time for availability.");
                    }
                    message(request, "Availability published for patients.");
                    destination = safeDestination(request, "/doctor/schedule");
                }
                case "/availability/delete" -> {
                    requireRole(user, "DOCTOR");
                    availability.delete(user, number(request, "availabilityId"));
                    message(request, "Availability removed.");
                    destination = safeDestination(request, "/doctor/schedule");
                }
                case "/settings/save" -> {
                    requireRole(user, "ADMIN");
                    String clinicName = value(request, "clinicName");
                    String clinicEmail = value(request, "clinicEmail");
                    String clinicPhone = value(request, "clinicPhone");
                    String clinicAddress = value(request, "clinicAddress");
                    int duration = Integer.parseInt(value(request, "appointmentDuration"));
                    if (clinicName.isBlank() || clinicName.length() > 120 || duration < 10 || duration > 180
                            || clinicEmail.length() > 190 || !clinicEmail.isEmpty()
                                    && !clinicEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                            || clinicPhone.length() > 40 || clinicAddress.length() > 250) {
                        throw new IllegalArgumentException("Enter valid clinic contact details and an appointment duration from 10 to 180 minutes.");
                    }
                    settings.save(clinicName, duration, clinicEmail, clinicPhone, clinicAddress);
                    message(request, "Clinic settings saved.");
                    destination = safeDestination(request, "/admin/settings");
                }
                case "/profile/save" -> {
                    requireRole(user, "PATIENT");
                    String name = value(request, "fullName");
                    String email = value(request, "email");
                    if (name.length() < 2 || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                        throw new IllegalArgumentException("Enter a name and valid email address.");
                    }
                    users.updatePatientProfile(user, name, email);
                    request.getSession().setAttribute("user", users.findById(user.id()));
                    message(request, "Your profile was updated.");
                    destination = safeDestination(request, "/patient/profile");
                }
                case "/profile/password" -> {
                    requireRole(user, "PATIENT");
                    String currentPassword = value(request, "currentPassword");
                    String newPassword = value(request, "newPassword");
                    String confirmPassword = value(request, "confirmPassword");
                    if (newPassword.length() < 8 || !newPassword.equals(confirmPassword)) {
                        throw new IllegalArgumentException("Enter a new password with at least 8 characters and confirm it correctly.");
                    }
                    users.changePatientPassword(user, currentPassword, newPassword);
                    message(request, "Password changed successfully.");
                    destination = safeDestination(request, "/patient/profile");
                }
                case "/feedback/create" -> {
                    requireRole(user, "PATIENT");
                    feedback.add(user, number(request, "doctorId"), Integer.parseInt(value(request, "rating")),
                            value(request, "comments"));
                    message(request, "Thank you for your feedback.");
                    destination = safeDestination(request, "/patient/feedback");
                }
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
            if (!response.isCommitted()) {
                response.sendRedirect(request.getContextPath() + destination);
            }
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (IllegalArgumentException exception) {
            request.getSession().setAttribute("flash", exception.getMessage());
            response.sendRedirect(request.getContextPath() + destination);
        } catch (SQLException exception) {
            getServletContext().log("Database action failed.", exception);
            request.getSession().setAttribute("flash", "The request could not be saved. Check the database and submitted details.");
            response.sendRedirect(request.getContextPath() + destination);
        }
    }

    private String safeDestination(HttpServletRequest request, String fallback) {
        String destination = request.getParameter("returnTo");
        if (destination == null || !destination.startsWith("/") || destination.startsWith("//")
                || destination.contains("\\") || destination.contains("..")) {
            return fallback;
        }
        return destination;
    }

    private String value(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }

    private long number(HttpServletRequest request, String name) {
        return Long.parseLong(value(request, name));
    }

    private LocalDateTime appointmentDateTime(HttpServletRequest request) {
        try {
            return LocalDateTime.parse(value(request, "appointmentAt"));
        } catch (DateTimeException exception) {
            throw new IllegalArgumentException("Choose a valid appointment date and time.");
        }
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

    private void message(HttpServletRequest request, String message) {
        request.getSession().setAttribute("flash", message);
    }
}