package com.healthcare.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Loads and saves the clinic-wide settings used by public pages and appointment booking. */
public class SettingRepository {
    /** Returns saved settings with application defaults for keys that are not yet stored. */
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
        settings.putIfAbsent("clinic_email", "");
        settings.putIfAbsent("clinic_phone", "");
        settings.putIfAbsent("clinic_address", "");
        return settings;
    }

    /** Saves all clinic settings together so a failed update cannot leave partial configuration. */
    public void save(String clinicName, int appointmentDuration, String clinicEmail, String clinicPhone,
            String clinicAddress) throws SQLException {
        String sql = "INSERT INTO system_settings (setting_key, setting_value) VALUES (?, ?) "
                + "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)";
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                // Save the clinic configuration as one unit so partial updates are not left behind on failure.
                saveValue(statement, "clinic_name", clinicName.trim());
                saveValue(statement, "appointment_duration", Integer.toString(appointmentDuration));
                saveValue(statement, "clinic_email", clinicEmail.trim());
                saveValue(statement, "clinic_phone", clinicPhone.trim());
                saveValue(statement, "clinic_address", clinicAddress.trim());
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    /** Binds and writes one key/value setting using the shared upsert statement. */
    private void saveValue(PreparedStatement statement, String key, String value) throws SQLException {
        statement.setString(1, key);
        statement.setString(2, value);
        statement.executeUpdate();
    }

    /** Returns the configured appointment length in minutes. */
    public int appointmentDuration() throws SQLException {
        return Integer.parseInt(getAll().get("appointment_duration"));
    }
}