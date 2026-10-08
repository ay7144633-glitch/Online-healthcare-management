package com.healthcare.model;

import java.time.LocalDateTime;

public record Feedback(String patientName, int rating, String comments, LocalDateTime createdAt) {
}