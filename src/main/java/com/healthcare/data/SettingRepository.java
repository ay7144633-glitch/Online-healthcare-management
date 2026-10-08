package com.healthcare.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class SettingRepository {
    public Map<String, String> getAll() throws SQLException {
        Map<String, String> settings = new LinkedHashMap<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT setting_key, setting_value FROM system_settings");
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                settings.put(results.getString("setting_key"), results.getString("setting_value"));
            }
        }
        settings.putIfAbsent("clinic_name", "Carepoint Clinic");
        settings.putIfAbsent("appointment_duration", "30");
        return settings;
    }

    public void save(String clinicName, int appointmentDuration) throws SQLException {
        String sql = "INSERT INTO system_settings (setting_key, setting_value) VALUES (?, ?) "
                + "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)";
        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "clinic_name");
            statement.setString(2, clinicName.trim());
            statement.executeUpdate();
            statement.setString(1, "appointment_duration");
            statement.setString(2, Integer.toString(appointmentDuration));
            statement.executeUpdate();
        }
    }

    public int appointmentDuration() throws SQLException {
        return Integer.parseInt(getAll().get("appointment_duration"));
    }
}