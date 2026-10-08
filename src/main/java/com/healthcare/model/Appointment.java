package com.healthcare.model;

import java.time.LocalDateTime;

public record Appointment(long id, long patientId, long doctorId, String patientName,
                          String doctorName, LocalDateTime appointmentAt, String status, String reason) {
}