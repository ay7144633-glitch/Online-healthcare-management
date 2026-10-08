package com.healthcare.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.healthcare.model.Availability;
import com.healthcare.model.User;

public class AvailabilityRepository {
    public List<Availability> listFor(User doctor) throws SQLException {
        String sql = "SELECT id, available_from, available_to, NULL AS doctor_name, NULL AS specialty "
            + "FROM doctor_availability WHERE doctor_id = ? ORDER BY available_from";
        List<Availability> blocks = new ArrayList<>();
        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, doctor.id());
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                        blocks.add(mapAvailability(results));
                }
            }
        }
        return blocks;
    }

    public List<Availability> listUpcoming() throws SQLException {
        String sql = "SELECT a.id, a.available_from, a.available_to, u.full_name AS doctor_name, u.specialty "
                + "FROM doctor_availability a JOIN users u ON u.id = a.doctor_id "
                + "WHERE u.active = TRUE AND a.available_to > NOW() ORDER BY a.available_from";
        List<Availability> blocks = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                blocks.add(mapAvailability(results));
            }
        }
        return blocks;
    }

    public void add(User doctor, LocalDateTime from, LocalDateTime to) throws SQLException {
        long minutes = Duration.between(from, to).toMinutes();
        if (!from.isAfter(LocalDateTime.now()) || minutes < 30 || minutes > 720) {
            throw new IllegalArgumentException("Availability must start in the future and last between 30 minutes and 12 hours.");
        }
        String sql = "INSERT INTO doctor_availability (doctor_id, available_from, available_to) VALUES (?, ?, ?)";
        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, doctor.id());
            statement.setTimestamp(2, Timestamp.valueOf(from));
            statement.setTimestamp(3, Timestamp.valueOf(to));
            statement.executeUpdate();
        }
    }

    public void delete(User doctor, long availabilityId) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM doctor_availability WHERE id = ? AND doctor_id = ?")) {
            statement.setLong(1, availabilityId);
            statement.setLong(2, doctor.id());
            if (statement.executeUpdate() != 1) {
                throw new IllegalArgumentException("Availability block not found.");
            }
        }
    }

    private Availability mapAvailability(ResultSet results) throws SQLException {
        return new Availability(results.getLong("id"), results.getTimestamp("available_from").toLocalDateTime(),
                results.getTimestamp("available_to").toLocalDateTime(), results.getString("doctor_name"),
                results.getString("specialty"));
    }
}