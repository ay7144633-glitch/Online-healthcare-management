package com.healthcare.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.healthcare.model.MedicalRecord;
import com.healthcare.model.User;

public class MedicalRecordRepository {
    public List<MedicalRecord> listFor(User user) throws SQLException {
        String scope = switch (user.role()) {
            case "PATIENT" -> " WHERE r.patient_id = ?";
            case "DOCTOR" -> " WHERE r.doctor_id = ?";
            default -> "";
        };
        String sql = "SELECT r.id, p.full_name AS patient_name, d.full_name AS doctor_name, "
                + "r.diagnosis, r.notes, r.created_at FROM medical_records r "
                + "JOIN users p ON p.id = r.patient_id JOIN users d ON d.id = r.doctor_id"
                + scope + " ORDER BY r.created_at DESC";
        List<MedicalRecord> records = new ArrayList<>();
        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            if (!scope.isEmpty()) {
                statement.setLong(1, user.id());
            }
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    records.add(new MedicalRecord(results.getLong("id"), results.getString("patient_name"),
                            results.getString("doctor_name"), results.getString("diagnosis"),
                            results.getString("notes"), results.getTimestamp("created_at").toLocalDateTime()));
                }
            }
        }
        return records;
    }

    public void add(User doctor, long appointmentId, String diagnosis, String notes) throws SQLException {
        String sql = "INSERT INTO medical_records (appointment_id, patient_id, doctor_id, diagnosis, notes) "
                + "SELECT a.id, a.patient_id, a.doctor_id, ?, ? FROM appointments a "
                + "WHERE a.id = ? AND a.doctor_id = ?";
        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, diagnosis.trim());
            statement.setString(2, notes.trim());
            statement.setLong(3, appointmentId);
            statement.setLong(4, doctor.id());
            if (statement.executeUpdate() != 1) {
                throw new IllegalArgumentException("Choose one of your appointments with this patient.");
            }
        }
    }

    public void update(User doctor, long recordId, String diagnosis, String notes) throws SQLException {
        String sql = "UPDATE medical_records SET diagnosis = ?, notes = ? WHERE id = ? AND doctor_id = ?";
        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, diagnosis.trim());
            statement.setString(2, notes.trim());
            statement.setLong(3, recordId);
            statement.setLong(4, doctor.id());
            if (statement.executeUpdate() != 1) {
                throw new IllegalArgumentException("Record not found in your care.");
            }
        }
    }
}