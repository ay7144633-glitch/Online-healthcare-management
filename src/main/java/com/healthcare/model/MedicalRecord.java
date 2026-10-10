package com.healthcare.model;

import java.time.LocalDateTime;

/** Represents a clinician's diagnosis and notes for a patient's visit. */
public record MedicalRecord(long id, String patientName, String doctorName, String diagnosis,
                            String notes, LocalDateTime createdAt) {
}