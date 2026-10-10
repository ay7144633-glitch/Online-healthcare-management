package com.healthcare.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import com.healthcare.model.Appointment;
import com.healthcare.model.MedicalRecord;
import com.healthcare.model.User;

/** Exercises appointment, account, patient-history, and settings flows against MySQL. */
@EnabledIfEnvironmentVariable(named = "DB_INTEGRATION_TESTS", matches = "true")
class AppointmentInteractionIntegrationTest {
    private final UserRepository users = new UserRepository();
    private final AvailabilityRepository availability = new AvailabilityRepository();
    private final AppointmentRepository appointments = new AppointmentRepository();
    private final MedicalRecordRepository records = new MedicalRecordRepository();
    private final PatientHistoryRepository patientHistory = new PatientHistoryRepository();
    private final SettingRepository settings = new SettingRepository();

    /** Verifies booking visibility, role actions, rescheduling conflicts, and linked records. */
    @Test
    void patientBookingAppearsForDoctorAndDoctorConfirmationAppearsForPatient() throws SQLException {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String patientEmail = "patient-" + suffix + "@integration.local";
        String doctorEmail = "doctor-" + suffix + "@integration.local";
        String deletableEmail = "deletable-" + suffix + "@integration.local";
        String deletableAdminEmail = "deletable-admin-" + suffix + "@integration.local";

        try {
            Database.initialize();
            users.createUser("Integration Patient", patientEmail, "test-password", "PATIENT", null);
            users.createUser("Integration Doctor", doctorEmail, "test-password", "DOCTOR", "Testing");
            users.createUser("Temporary Doctor", deletableEmail, "test-password", "DOCTOR", "Testing");
            users.createUser("Temporary Admin", deletableAdminEmail, "test-password", "ADMIN", null);
            User patient = findUser(patientEmail);
            User doctor = findUser(doctorEmail);
            User deletable = findUser(deletableEmail);
            User deletableAdmin = findUser(deletableAdminEmail);

            LocalDateTime appointmentAt = LocalDateTime.now().plusDays(2).withHour(10)
                    .withMinute(30).withSecond(0).withNano(0);
            availability.add(doctor, appointmentAt.minusMinutes(30), appointmentAt.plusHours(6));
            appointments.book(patient.id(), doctor.id(), appointmentAt, "Integration test visit", 30);

            Appointment booked = appointments.listFor(doctor).stream()
                    .filter(appointment -> appointment.reason().equals("Integration test visit"))
                    .findFirst().orElseThrow();
            assertEquals("PENDING", booked.status());
            assertTrue(appointments.listFor(patient).stream()
                    .anyMatch(appointment -> appointment.id() == booked.id()));

            appointments.updateStatus(doctor, booked.id(), "CONFIRMED");

            assertEquals("CONFIRMED", appointments.listFor(patient).stream()
                    .filter(appointment -> appointment.id() == booked.id())
                    .findFirst().orElseThrow().status());

                LocalDateTime rescheduledAt = appointmentAt.plusHours(2);
                appointments.reschedule(patient, booked.id(), rescheduledAt);
                assertEquals(rescheduledAt, appointments.listFor(doctor).stream()
                    .filter(appointment -> appointment.id() == booked.id())
                    .findFirst().orElseThrow().appointmentAt());
                    appointments.book(patient.id(), doctor.id(), appointmentAt.plusHours(3), "Conflict test visit", 30);
                        assertEquals("That time conflicts with another appointment.", assertThrows(IllegalArgumentException.class,
                            () -> appointments.reschedule(patient, booked.id(), appointmentAt.plusHours(3))).getMessage());
                    assertEquals(rescheduledAt, appointments.listFor(patient).stream()
                        .filter(appointment -> appointment.id() == booked.id())
                        .findFirst().orElseThrow().appointmentAt());

            appointments.book(patient.id(), doctor.id(), appointmentAt.plusHours(4),
                            "Administrator scheduled visit", 30);
            Appointment adminScheduled = appointments.listFor(deletableAdmin).stream()
                            .filter(appointment -> appointment.reason().equals("Administrator scheduled visit"))
                            .findFirst().orElseThrow();
            assertTrue(appointments.listFor(patient).stream()
                            .anyMatch(appointment -> appointment.reason().equals("Administrator scheduled visit")));
            LocalDateTime adminRescheduledAt = appointmentAt.plusHours(4).plusMinutes(30);
            appointments.reschedule(deletableAdmin, adminScheduled.id(), adminRescheduledAt);
            assertEquals(adminRescheduledAt, appointments.listFor(patient).stream()
                            .filter(appointment -> appointment.id() == adminScheduled.id())
                            .findFirst().orElseThrow().appointmentAt());

                records.add(doctor, booked.id(), "Initial diagnosis", "Initial visit note");
                MedicalRecord record = records.listFor(doctor).stream()
                    .filter(item -> item.diagnosis().equals("Initial diagnosis"))
                    .findFirst().orElseThrow();
                records.update(doctor, record.id(), "Updated diagnosis", "Updated visit note");
                assertEquals("Updated visit note", records.listFor(patient).stream()
                    .filter(item -> item.id() == record.id())
                    .findFirst().orElseThrow().notes());
                        assertEquals("Record not found in your care.", assertThrows(IllegalArgumentException.class,
                            () -> records.update(deletable, record.id(), "Unauthorized edit", "Should be rejected")).getMessage());

                        assertEquals("Current password is incorrect.", assertThrows(IllegalArgumentException.class,
                            () -> users.changePatientPassword(patient, "wrong-current-password", "replacement-password")).getMessage());
                users.changePatientPassword(patient, "test-password", "replacement-password");
                assertEquals(patient.id(), users.authenticate(patientEmail, "replacement-password").id());
                    assertNull(users.authenticate(patientEmail, "test-password"));
                assertEquals("Updated Integration Patient", updatePatientAccount(patient, patientEmail));
                        assertEquals("This account has linked clinic history, so its role cannot be changed.",
                            assertThrows(IllegalArgumentException.class,
                                () -> users.updateAccount(doctor.id(), "Changed Role", doctorEmail, "PATIENT", null)).getMessage());
                        assertEquals("This account has linked clinic history. Deactivate it instead of deleting it.",
                            assertThrows(IllegalArgumentException.class, () -> users.deleteAccount(doctor.id())).getMessage());

                            patientHistory.add(patient, "Medication", "Daily medication for integration testing.");
                            var historyEntry = patientHistory.listFor(patient).stream()
                                .filter(entry -> entry.title().equals("Medication"))
                                .findFirst().orElseThrow();
                            patientHistory.update(patient, historyEntry.id(), "Allergy", "Updated patient-entered history.");
                            assertEquals("Updated patient-entered history.", patientHistory.listFor(patient).stream()
                                .filter(entry -> entry.id() == historyEntry.id())
                                .findFirst().orElseThrow().details());
                            assertTrue(patientHistory.listFor(doctor).isEmpty());
                                assertEquals("Health history entry not found.", assertThrows(IllegalArgumentException.class,
                                    () -> patientHistory.delete(doctor, historyEntry.id())).getMessage());
                            patientHistory.delete(patient, historyEntry.id());
                            assertTrue(patientHistory.listFor(patient).isEmpty());

                users.deleteAccount(deletable.id());
                assertFalse(users.existsByEmail(deletableEmail));
                users.deleteAccount(deletableAdmin.id());
                assertFalse(users.existsByEmail(deletableAdminEmail));
                    assertEquals(6, appointments.monthlyAnalytics().size());
                    assertTrue(appointments.monthlyAnalytics().stream().mapToLong(point -> point.count()).sum() >= 2);
                    assertEquals(6, users.monthlyRegistrationAnalytics().size());
                    assertTrue(users.monthlyRegistrationAnalytics().stream().mapToLong(point -> point.count()).sum() >= 2);
        } finally {
                deleteTestData(patientEmail, doctorEmail, deletableEmail, deletableAdminEmail);
        }
    }

    /** Verifies clinic settings persist through JDBC and restores the original values afterward. */
    @Test
    void clinicSettingsPersistAndRestoreThroughJdbc() throws SQLException {
        Database.initialize();
        Map<String, String> original = settings.getAll();
        try {
            settings.save("Integration Clinic", 45, "contact@integration.local",
                    "+1 555 0100", "1 Test Street");

            Map<String, String> saved = settings.getAll();
            assertEquals("Integration Clinic", saved.get("clinic_name"));
            assertEquals("45", saved.get("appointment_duration"));
            assertEquals("contact@integration.local", saved.get("clinic_email"));
            assertEquals("+1 555 0100", saved.get("clinic_phone"));
            assertEquals("1 Test Street", saved.get("clinic_address"));
        } finally {
            settings.save(original.get("clinic_name"), Integer.parseInt(original.get("appointment_duration")),
                    original.get("clinic_email"), original.get("clinic_phone"), original.get("clinic_address"));
        }
    }

    /** Loads a test account by email and maps it to the user model. */
    private User findUser(String email) throws SQLException {
        String sql = "SELECT id, full_name, email, role, specialty, active FROM users WHERE email = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    throw new SQLException("Integration test account was not created.");
                }
                return new User(results.getLong("id"), results.getString("full_name"),
                        results.getString("email"), results.getString("role"),
                        results.getString("specialty"), results.getBoolean("active"));
            }
        }
    }

    /** Updates a patient's account and returns the persisted display name. */
    private String updatePatientAccount(User patient, String email) throws SQLException {
        users.updateAccount(patient.id(), "Updated Integration Patient", email, "PATIENT", null);
        return findUser(email).fullName();
    }

    /** Removes all database rows created by the integration workflow. */
    private void deleteTestData(String patientEmail, String doctorEmail, String deletableEmail,
            String deletableAdminEmail) throws SQLException {
        String[] emails = {patientEmail, doctorEmail, deletableEmail, deletableAdminEmail};
        try (Connection connection = Database.getConnection()) {
            deleteByEmails(connection, "DELETE FROM patient_health_history WHERE patient_id IN ", emails);
            deleteByEmails(connection, "DELETE FROM medical_records WHERE patient_id IN ", emails);
            deleteByEmails(connection, "DELETE FROM medical_records WHERE doctor_id IN ", emails);
            deleteByEmails(connection, "DELETE FROM feedback WHERE patient_id IN ", emails);
            deleteByEmails(connection, "DELETE FROM feedback WHERE doctor_id IN ", emails);
            deleteByEmails(connection, "DELETE FROM appointments WHERE patient_id IN ", emails);
            deleteByEmails(connection, "DELETE FROM appointments WHERE doctor_id IN ", emails);
            deleteByEmails(connection, "DELETE FROM doctor_availability WHERE doctor_id IN ", emails);
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE email IN (?, ?, ?, ?)")) {
                for (int index = 0; index < emails.length; index++) {
                    statement.setString(index + 1, emails[index]);
                }
                statement.executeUpdate();
            }
        }
    }

    /** Deletes rows whose patient or doctor ID belongs to one of the test accounts. */
    private void deleteByEmails(Connection connection, String prefix, String... emails) throws SQLException {
        String placeholders = String.join(", ", Arrays.stream(emails).map(email -> "?").toList());
        String sql = prefix + "(SELECT id FROM users WHERE email IN (" + placeholders + "))";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < emails.length; index++) {
                statement.setString(index + 1, emails[index]);
            }
            statement.executeUpdate();
        }
    }
}