package com.healthcare.model;

import java.time.LocalDateTime;

public record PatientHistoryEntry(long id, String title, String details, LocalDateTime updatedAt) {
}
