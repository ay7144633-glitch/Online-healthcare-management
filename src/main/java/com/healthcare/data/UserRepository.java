package com.healthcare.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.healthcare.model.AnalyticsPoint;
import com.healthcare.model.User;
import com.healthcare.util.PasswordService;

/** Manages user accounts, authentication, patient profiles, and registration analytics. */
public class UserRepository {
    /** Returns user-registration counts for the latest six months, including empty months. */
    public List<AnalyticsPoint> monthlyRegistrationAnalytics() throws SQLException {
        YearMonth currentMonth = YearMonth.now();
        YearMonth firstMonth = currentMonth.minusMonths(5);
        Map<YearMonth, Long> counts = new HashMap<>();
        String sql = "SELECT YEAR(created_at) AS year_value, MONTH(created_at) AS month_value, COUNT(*) AS total "
                + "FROM users WHERE created_at >= ? AND created_at < ? "
                + "GROUP BY YEAR(created_at), MONTH(created_at)";
        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(firstMonth.atDay(1).atStartOfDay()));
            statement.setTimestamp(2, Timestamp.valueOf(currentMonth.plusMonths(1).atDay(1).atStartOfDay()));
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    counts.put(YearMonth.of(results.getInt("year_value"), results.getInt("month_value")),
                            results.getLong("total"));
                }
            }
        }
        long maximum = counts.values().stream().mapToLong(Long::longValue).max().orElse(0);
        List<AnalyticsPoint> points = new ArrayList<>();
        DateTimeFormatter labelFormat = DateTimeFormatter.ofPattern("MMM yy", Locale.ENGLISH);
        for (int offset = 0; offset < 6; offset++) {
            YearMonth month = firstMonth.plusMonths(offset);
            long count = counts.getOrDefault(month, 0L);
            int percentage = maximum == 0 ? 0 : (int) Math.round(count * 100.0 / maximum);
            points.add(new AnalyticsPoint(month.format(labelFormat), count, percentage));
        }
        return points;
    }

    /** Authenticates an active account by email and password, or returns null on failure. */
    public User authenticate(String email, String password) throws SQLException {
        String sql = "SELECT id, full_name, email, password_hash, role, specialty, active FROM users WHERE email = ? AND active = TRUE";
        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email.trim().toLowerCase());
            try (ResultSet results = statement.executeQuery()) {
                if (results.next() && PasswordService.verify(password, results.getString("password_hash"))) {
                    return mapUser(results);
                }
            }
        }
        return null;
    }

    /** Creates a patient account through the standard user-creation path. */
    public void registerPatient(String fullName, String email, String password) throws SQLException {
        createUser(fullName, email, password, "PATIENT", null);
    }

    /** Creates an account after normalizing its email and hashing its password. */
    public void createUser(String fullName, String email, String password, String role, String specialty) throws SQLException {
        String sql = "INSERT INTO users (full_name, email, password_hash, role, specialty) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, fullName.trim());
            statement.setString(2, email.trim().toLowerCase());
            statement.setString(3, PasswordService.hash(password));
            statement.setString(4, role);
            statement.setString(5, specialty == null || specialty.isBlank() ? null : specialty.trim());
            statement.executeUpdate();
        }
    }

    /** Checks whether an account already uses the supplied email address. */
    public boolean existsByEmail(String email) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM users WHERE email = ?")) {
            statement.setString(1, email.trim().toLowerCase());
            try (ResultSet results = statement.executeQuery()) {
                return results.next();
            }
        }
    }

    /** Lists active doctor accounts for appointment booking and feedback forms. */
    public List<User> listDoctors() throws SQLException {
        return queryUsers("SELECT id, full_name, email, role, specialty, active FROM users WHERE role = 'DOCTOR' AND active = TRUE ORDER BY full_name");
    }

    /** Lists active patient accounts for administrator scheduling. */
    public List<User> listPatients() throws SQLException {
        return queryUsers("SELECT id, full_name, email, role, specialty, active FROM users WHERE role = 'PATIENT' AND active = TRUE ORDER BY full_name");
    }

    /** Finds an active account by its database ID, or returns null if none exists. */
    public User findById(long id) throws SQLException {
        String sql = "SELECT id, full_name, email, role, specialty, active FROM users WHERE id = ? AND active = TRUE";
        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? mapUser(results) : null;
            }
        }
    }

    /** Lists all accounts, including inactive accounts, for administrator management. */
    public List<User> listAll() throws SQLException {
        return queryUsers("SELECT id, full_name, email, role, specialty, active FROM users ORDER BY created_at DESC");
    }

    /** Activates or deactivates a non-administrator account. */
    public void setActive(long id, boolean active) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE users SET active = ? WHERE id = ? AND role <> 'ADMIN'")) {
            statement.setBoolean(1, active);
            statement.setLong(2, id);
            statement.executeUpdate();
        }
    }

    /** Updates account details while preventing role changes that would orphan clinic history. */
    public void updateAccount(long id, String fullName, String email, String role, String specialty) throws SQLException {
        if (!List.of("ADMIN", "DOCTOR", "PATIENT").contains(role)) {
            throw new IllegalArgumentException("Choose a valid account role.");
        }
        try (Connection connection = Database.getConnection()) {
            String currentRole;
            try (PreparedStatement statement = connection.prepareStatement("SELECT role FROM users WHERE id = ?")) {
                statement.setLong(1, id);
                try (ResultSet results = statement.executeQuery()) {
                    if (!results.next()) {
                        throw new IllegalArgumentException("Account not found.");
                    }
                    currentRole = results.getString("role");
                }
            }
            // Keep historical appointments and records tied to the role under which they were created.
            if (!currentRole.equals(role) && hasClinicHistory(connection, id)) {
                throw new IllegalArgumentException("This account has linked clinic history, so its role cannot be changed.");
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE users SET full_name = ?, email = ?, role = ?, specialty = ? WHERE id = ?")) {
                statement.setString(1, fullName.trim());
                statement.setString(2, email.trim().toLowerCase());
                statement.setString(3, role);
                statement.setString(4, role.equals("DOCTOR") && specialty != null && !specialty.isBlank()
                        ? specialty.trim() : null);
                statement.setLong(5, id);
                if (statement.executeUpdate() != 1) {
                    throw new IllegalArgumentException("Account not found.");
                }
            }
        }
    }

    /** Deletes an account only when it has no linked clinic history. */
    public void deleteAccount(long id) throws SQLException {
        try (Connection connection = Database.getConnection()) {
            // Preserve referenced clinic history; deactivation is the safe alternative for accounts with records.
            if (hasClinicHistory(connection, id)) {
                throw new IllegalArgumentException("This account has linked clinic history. Deactivate it instead of deleting it.");
            }
                try (PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE id = ?")) {
                statement.setLong(1, id);
                if (statement.executeUpdate() != 1) {
                    throw new IllegalArgumentException("Account not found or administrator accounts cannot be deleted here.");
                }
            }
        }
    }

    /** Updates the name and email on the specified patient's own profile. */
    public void updatePatientProfile(User user, String fullName, String email) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, email = ? WHERE id = ? AND role = 'PATIENT'";
        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, fullName.trim());
            statement.setString(2, email.trim().toLowerCase());
            statement.setLong(3, user.id());
            statement.executeUpdate();
        }
    }

    /** Verifies the current password before storing a new hash for the patient. */
    public void changePatientPassword(User user, String currentPassword, String newPassword) throws SQLException {
        String storedHash;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT password_hash FROM users WHERE id = ? AND role = 'PATIENT' AND active = TRUE")) {
            statement.setLong(1, user.id());
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    throw new IllegalArgumentException("Patient account not found.");
                }
                storedHash = results.getString("password_hash");
            }
        }
        if (!PasswordService.verify(currentPassword, storedHash)) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE users SET password_hash = ? WHERE id = ? AND role = 'PATIENT'")) {
            statement.setString(1, PasswordService.hash(newPassword));
            statement.setLong(2, user.id());
            statement.executeUpdate();
        }
    }

    /** Executes an account query and maps every returned row to a user model. */
    private List<User> queryUsers(String sql) throws SQLException {
        List<User> users = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                users.add(mapUser(results));
            }
        }
        return users;
    }

    /** Checks whether appointments, availability, records, or feedback reference this account. */
    private boolean hasClinicHistory(Connection connection, long userId) throws SQLException {
        String sql = "SELECT EXISTS(SELECT 1 FROM appointments WHERE patient_id = ? OR doctor_id = ?) "
                + "OR EXISTS(SELECT 1 FROM doctor_availability WHERE doctor_id = ?) "
                + "OR EXISTS(SELECT 1 FROM medical_records WHERE patient_id = ? OR doctor_id = ?) "
                + "OR EXISTS(SELECT 1 FROM feedback WHERE patient_id = ? OR doctor_id = ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 1; index <= 7; index++) {
                statement.setLong(index, userId);
            }
            try (ResultSet results = statement.executeQuery()) {
                results.next();
                return results.getBoolean(1);
            }
        }
    }

    /** Converts the current database row into a user model. */
    private User mapUser(ResultSet results) throws SQLException {
        return new User(results.getLong("id"), results.getString("full_name"), results.getString("email"),
            results.getString("role"), results.getString("specialty"), results.getBoolean("active"));
    }
}