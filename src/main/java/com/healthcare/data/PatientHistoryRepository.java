package com.healthcare.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.healthcare.model.PatientHistoryEntry;
import com.healthcare.model.User;

public class PatientHistoryRepository {
    public List<PatientHistoryEntry> listFor(User patient) throws SQLException {
        String sql = "SELECT id, title, details, updated_at FROM patient_health_history "
                + "WHERE patient_id = ? ORDER BY updated_at DESC";
        List<PatientHistoryEntry> entries = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, patient.id());
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    entries.add(new PatientHistoryEntry(results.getLong("id"), results.getString("title"),
                            results.getString("details"), results.getTimestamp("updated_at").toLocalDateTime()));
                }
            }
        }
        return entries;
    }

    public void add(User patient, String title, String details) throws SQLException {
        validate(title, details);
        String sql = "INSERT INTO patient_health_history (patient_id, title, details) VALUES (?, ?, ?)";
        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, patient.id());
            statement.setString(2, title.trim());
            statement.setString(3, details.trim());
            statement.executeUpdate();
        }
    }

    public void update(User patient, long entryId, String title, String details) throws SQLException {
        validate(title, details);
        String sql = "UPDATE patient_health_history SET title = ?, details = ? WHERE id = ? AND patient_id = ?";
        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, title.trim());
            statement.setString(2, details.trim());
            statement.setLong(3, entryId);
            statement.setLong(4, patient.id());
            if (statement.executeUpdate() != 1) {
                throw new IllegalArgumentException("Health history entry not found.");
            }
        }
    }

    public void delete(User patient, long entryId) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM patient_health_history WHERE id = ? AND patient_id = ?")) {
            statement.setLong(1, entryId);
            statement.setLong(2, patient.id());
            if (statement.executeUpdate() != 1) {
                throw new IllegalArgumentException("Health history entry not found.");
            }
        }
    }

    private void validate(String title, String details) {
        if (title.isBlank() || title.length() > 160 || details.isBlank() || details.length() > 5000) {
            throw new IllegalArgumentException("Enter a title and details (up to 5,000 characters).");
        }
    }
}
