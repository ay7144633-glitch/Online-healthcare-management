package com.healthcare.model;

import java.time.LocalDateTime;

/** Represents a health-history note entered and maintained by a patient. */
public record PatientHistoryEntry(long id, String title, String details, LocalDateTime updatedAt) {
}
