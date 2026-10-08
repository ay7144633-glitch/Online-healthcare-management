package com.healthcare.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        String host = System.getenv().getOrDefault("DB_HOST", "localhost");
        String port = System.getenv().getOrDefault("DB_PORT", "3306");
        String name = System.getenv().getOrDefault("DB_NAME", "healthcare_management");
        String url = "jdbc:mysql://" + host + ":" + port + "/" + name
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String user = System.getenv().getOrDefault("DB_USER", "root");
        String password = System.getenv().getOrDefault("DB_PASSWORD", "");
        return DriverManager.getConnection(url, user, password);
    }

    public static void initialize() throws SQLException {
        String[] statements = {
            "CREATE TABLE IF NOT EXISTS users (id BIGINT PRIMARY KEY AUTO_INCREMENT, full_name VARCHAR(120) NOT NULL, email VARCHAR(190) NOT NULL UNIQUE, password_hash VARCHAR(255) NOT NULL, role ENUM('ADMIN','DOCTOR','PATIENT') NOT NULL, specialty VARCHAR(120), active BOOLEAN NOT NULL DEFAULT TRUE, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)",
            "CREATE TABLE IF NOT EXISTS appointments (id BIGINT PRIMARY KEY AUTO_INCREMENT, patient_id BIGINT NOT NULL, doctor_id BIGINT NOT NULL, appointment_at DATETIME NOT NULL, duration_minutes SMALLINT NOT NULL DEFAULT 30, status ENUM('PENDING','CONFIRMED','COMPLETED','CANCELLED') NOT NULL DEFAULT 'PENDING', reason VARCHAR(500) NOT NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, INDEX idx_appointment_doctor_time (doctor_id, appointment_at), CONSTRAINT fk_appointment_patient FOREIGN KEY (patient_id) REFERENCES users(id), CONSTRAINT fk_appointment_doctor FOREIGN KEY (doctor_id) REFERENCES users(id))",
            "CREATE TABLE IF NOT EXISTS doctor_availability (id BIGINT PRIMARY KEY AUTO_INCREMENT, doctor_id BIGINT NOT NULL, available_from DATETIME NOT NULL, available_to DATETIME NOT NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, INDEX idx_availability_doctor_time (doctor_id, available_from, available_to), CONSTRAINT fk_availability_doctor FOREIGN KEY (doctor_id) REFERENCES users(id))",
            "CREATE TABLE IF NOT EXISTS medical_records (id BIGINT PRIMARY KEY AUTO_INCREMENT, appointment_id BIGINT, patient_id BIGINT NOT NULL, doctor_id BIGINT NOT NULL, diagnosis VARCHAR(255) NOT NULL, notes TEXT NOT NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, CONSTRAINT fk_record_patient FOREIGN KEY (patient_id) REFERENCES users(id), CONSTRAINT fk_record_doctor FOREIGN KEY (doctor_id) REFERENCES users(id), CONSTRAINT fk_record_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id))",
            "CREATE TABLE IF NOT EXISTS patient_health_history (id BIGINT PRIMARY KEY AUTO_INCREMENT, patient_id BIGINT NOT NULL, title VARCHAR(160) NOT NULL, details TEXT NOT NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, INDEX idx_patient_history_updated (patient_id, updated_at), CONSTRAINT fk_patient_history_user FOREIGN KEY (patient_id) REFERENCES users(id))",
            "CREATE TABLE IF NOT EXISTS feedback (id BIGINT PRIMARY KEY AUTO_INCREMENT, patient_id BIGINT NOT NULL, doctor_id BIGINT NOT NULL, rating TINYINT NOT NULL, comments VARCHAR(1000), created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, CONSTRAINT fk_feedback_patient FOREIGN KEY (patient_id) REFERENCES users(id), CONSTRAINT fk_feedback_doctor FOREIGN KEY (doctor_id) REFERENCES users(id))",
            "CREATE TABLE IF NOT EXISTS system_settings (setting_key VARCHAR(100) PRIMARY KEY, setting_value VARCHAR(500) NOT NULL)"
        };
        try (Connection connection = getConnection(); var statement = connection.createStatement()) {
            for (String sql : statements) {
                statement.executeUpdate(sql);
            }
        }
    }
}