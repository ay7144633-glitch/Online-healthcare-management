package com.healthcare.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.healthcare.model.Feedback;
import com.healthcare.model.User;

public class FeedbackRepository {
    public void add(User patient, long doctorId, int rating, String comments) throws SQLException {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Choose a rating from 1 to 5.");
        }
        String sql = "INSERT INTO feedback (patient_id, doctor_id, rating, comments) "
                + "SELECT ?, doctor_id, ?, ? FROM appointments WHERE patient_id = ? AND doctor_id = ? AND status = 'COMPLETED' LIMIT 1";
        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, patient.id());
            statement.setInt(2, rating);
            statement.setString(3, comments.trim());
            statement.setLong(4, patient.id());
            statement.setLong(5, doctorId);
            if (statement.executeUpdate() != 1) {
                throw new IllegalArgumentException("Feedback is available after a completed appointment.");
            }
        }
    }

    public double averageFor(long doctorId) throws SQLException {
        String sql = "SELECT AVG(rating) AS average_rating FROM feedback WHERE doctor_id = ?";
        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, doctorId);
            try (ResultSet results = statement.executeQuery()) {
                results.next();
                return results.getDouble("average_rating");
            }
        }
    }

    public long countFor(long doctorId) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM feedback WHERE doctor_id = ?")) {
            statement.setLong(1, doctorId);
            try (ResultSet results = statement.executeQuery()) {
                results.next();
                return results.getLong(1);
            }
        }
    }

    public List<Feedback> listForDoctor(long doctorId) throws SQLException {
        String sql = "SELECT p.full_name, f.rating, f.comments, f.created_at FROM feedback f "
                + "JOIN users p ON p.id = f.patient_id WHERE f.doctor_id = ? ORDER BY f.created_at DESC LIMIT 5";
        List<Feedback> feedback = new ArrayList<>();
        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, doctorId);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    feedback.add(new Feedback(results.getString("full_name"), results.getInt("rating"),
                            results.getString("comments"), results.getTimestamp("created_at").toLocalDateTime()));
                }
            }
        }
        return feedback;
    }
}