package com.healthcare.model;

import java.time.LocalDateTime;

/** Represents a doctor's published availability window and display details. */
public record Availability(long id, LocalDateTime availableFrom, LocalDateTime availableTo,
                           String doctorName, String specialty) {
}