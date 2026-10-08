package com.healthcare.model;

import java.time.LocalDateTime;

public record MedicalRecord(long id, String patientName, String doctorName, String diagnosis,
                            String notes, LocalDateTime createdAt) {
}