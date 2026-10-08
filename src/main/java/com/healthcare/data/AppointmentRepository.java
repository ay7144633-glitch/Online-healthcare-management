package com.healthcare.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.healthcare.model.AnalyticsPoint;
import com.healthcare.model.Appointment;
import com.healthcare.model.User;

public class AppointmentRepository {
    public List<AnalyticsPoint> monthlyAnalytics() throws SQLException {
        YearMonth currentMonth = YearMonth.now();
        YearMonth firstMonth = currentMonth.minusMonths(5);
        Map<YearMonth, Long> counts = new HashMap<>();
        String sql = "SELECT YEAR(appointment_at) AS year_value, MONTH(appointment_at) AS month_value, COUNT(*) AS total "
                + "FROM appointments WHERE appointment_at >= ? AND appointment_at < ? "
                + "GROUP BY YEAR(appointment_at), MONTH(appointment_at)";
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

    public List<Appointment> listFor(User user) throws SQLException {
        String scope = switch (user.role()) {
            case "PATIENT" -> " WHERE a.patient_id = ?";
            case "DOCTOR" -> " WHERE a.doctor_id = ?";
            default -> "";
        };
        String sql = "SELECT a.id, a.patient_id, a.doctor_id, p.full_name AS patient_name, "
                + "d.full_name AS doctor_name, a.appointment_at, a.status, a.reason "
                + "FROM appointments a JOIN users p ON p.id = a.patient_id "
                + "JOIN users d ON d.id = a.doctor_id" + scope + " ORDER BY a.appointment_at DESC";
        List<Appointment> appointments = new ArrayList<>();
        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            if (!scope.isEmpty()) {
                statement.setLong(1, user.id());
            }
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    appointments.add(new Appointment(results.getLong("id"), results.getLong("patient_id"),
                            results.getLong("doctor_id"), results.getString("patient_name"),
                            results.getString("doctor_name"), results.getTimestamp("appointment_at").toLocalDateTime(),
                            results.getString("status"), results.getString("reason")));
                }
            }
        }
        return appointments;
    }

    public void book(long patientId, long doctorId, LocalDateTime appointmentAt, String reason, int durationMinutes) throws SQLException {
        if (!appointmentAt.isAfter(LocalDateTime.now()) || durationMinutes < 10 || durationMinutes > 180) {
            throw new IllegalArgumentException("Choose a future appointment time.");
        }
        LocalDateTime appointmentEnd = appointmentAt.plusMinutes(durationMinutes);
        String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_at, duration_minutes, reason) "
                + "SELECT ?, d.id, ?, ?, ? FROM users d WHERE d.id = ? AND d.role = 'DOCTOR' AND d.active = TRUE "
            // Require a published window and reject conflicting appointments for this clinician.
                + "AND EXISTS (SELECT 1 FROM doctor_availability v WHERE v.doctor_id = d.id AND ? >= v.available_from AND ? <= v.available_to) "
                + "AND NOT EXISTS (SELECT 1 FROM appointments a WHERE a.doctor_id = d.id AND a.status <> 'CANCELLED' "
                + "AND a.appointment_at < ? AND TIMESTAMPADD(MINUTE, a.duration_minutes, a.appointment_at) > ?)";
        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, patientId);
            statement.setTimestamp(2, Timestamp.valueOf(appointmentAt));
            statement.setInt(3, durationMinutes);
            statement.setString(4, reason.trim());
            statement.setLong(5, doctorId);
            statement.setTimestamp(6, Timestamp.valueOf(appointmentAt));
            statement.setTimestamp(7, Timestamp.valueOf(appointmentEnd));
            statement.setTimestamp(8, Timestamp.valueOf(appointmentEnd));
            statement.setTimestamp(9, Timestamp.valueOf(appointmentAt));
            if (statement.executeUpdate() != 1) {
                throw new IllegalArgumentException("Choose an open time inside the doctor's published availability.");
            }
        }
    }

    public void updateStatus(User actor, long appointmentId, String status) throws SQLException {
        if (!List.of("CONFIRMED", "COMPLETED", "CANCELLED").contains(status)) {
            throw new IllegalArgumentException("Choose a valid appointment status.");
        }
        if (actor.role().equals("PATIENT") && !status.equals("CANCELLED")) {
            throw new IllegalArgumentException("Patients may only cancel appointments.");
        }
        String scope = switch (actor.role()) {
            case "DOCTOR" -> " AND doctor_id = ?";
            case "PATIENT" -> " AND patient_id = ?";
            default -> "";
        };
        String sql = "UPDATE appointments SET status = ? WHERE id = ?" + scope;
        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setLong(2, appointmentId);
            if (!scope.isEmpty()) {
                statement.setLong(3, actor.id());
            }
            if (statement.executeUpdate() != 1) {
                throw new IllegalArgumentException("Appointment not found.");
            }
        }
    }

    public void reschedule(User actor, long appointmentId, LocalDateTime appointmentAt) throws SQLException {
        if (!appointmentAt.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Choose a future appointment time.");
        }
        String scope = switch (actor.role()) {
            case "PATIENT" -> " AND patient_id = ?";
            case "DOCTOR" -> " AND doctor_id = ?";
            case "ADMIN" -> "";
            default -> throw new SecurityException("Role not permitted.");
        };
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long doctorId;
                int duration;
                String status;
                String lookupSql = "SELECT doctor_id, duration_minutes, status FROM appointments WHERE id = ?" + scope + " FOR UPDATE";
                try (PreparedStatement statement = connection.prepareStatement(lookupSql)) {
                    statement.setLong(1, appointmentId);
                    if (!scope.isEmpty()) {
                        statement.setLong(2, actor.id());
                    }
                    try (ResultSet results = statement.executeQuery()) {
                        if (!results.next()) {
                            throw new IllegalArgumentException("Appointment not found.");
                        }
                        doctorId = results.getLong("doctor_id");
                        duration = results.getInt("duration_minutes");
                        status = results.getString("status");
                    }
                }
                if (!List.of("PENDING", "CONFIRMED").contains(status)) {
                    throw new IllegalArgumentException("Only pending or confirmed appointments can be rescheduled.");
                }

                Timestamp start = Timestamp.valueOf(appointmentAt);
                Timestamp end = Timestamp.valueOf(appointmentAt.plusMinutes(duration));
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT 1 FROM doctor_availability WHERE doctor_id = ? AND available_from <= ? AND available_to >= ? LIMIT 1")) {
                    statement.setLong(1, doctorId);
                    statement.setTimestamp(2, start);
                    statement.setTimestamp(3, end);
                    try (ResultSet results = statement.executeQuery()) {
                        if (!results.next()) {
                            throw new IllegalArgumentException("Choose a time inside the doctor's published availability.");
                        }
                    }
                }

                String conflictSql = "SELECT 1 FROM appointments WHERE doctor_id = ? AND id <> ? "
                        + "AND status <> 'CANCELLED' AND appointment_at < ? "
                        + "AND TIMESTAMPADD(MINUTE, duration_minutes, appointment_at) > ? LIMIT 1";
                try (PreparedStatement statement = connection.prepareStatement(conflictSql)) {
                    statement.setLong(1, doctorId);
                    statement.setLong(2, appointmentId);
                    statement.setTimestamp(3, end);
                    statement.setTimestamp(4, start);
                    try (ResultSet results = statement.executeQuery()) {
                        if (results.next()) {
                            throw new IllegalArgumentException("That time conflicts with another appointment.");
                        }
                    }
                }

                String updateSql = "UPDATE appointments SET appointment_at = ? WHERE id = ? "
                        + "AND status IN ('PENDING', 'CONFIRMED')" + scope;
                try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
                    statement.setTimestamp(1, start);
                    statement.setLong(2, appointmentId);
                    if (!scope.isEmpty()) {
                        statement.setLong(3, actor.id());
                    }
                    if (statement.executeUpdate() != 1) {
                        throw new IllegalArgumentException("Appointment could not be rescheduled.");
                    }
                }
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }
}