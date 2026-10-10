package com.healthcare.model;

import java.time.LocalDateTime;

/** Represents an appointment and the patient and doctor associated with it. */
public record Appointment(long id, long patientId, long doctorId, String patientName,
                          String doctorName, LocalDateTime appointmentAt, String status, String reason) {
}