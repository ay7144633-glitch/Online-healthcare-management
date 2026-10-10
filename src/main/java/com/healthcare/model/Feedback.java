package com.healthcare.model;

import java.time.LocalDateTime;

/** Represents a patient's rating and optional comment about a doctor. */
public record Feedback(String patientName, int rating, String comments, LocalDateTime createdAt) {
}