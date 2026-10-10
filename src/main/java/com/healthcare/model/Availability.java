package com.healthcare.model;

import java.time.LocalDateTime;

public record Availability(long id, LocalDateTime availableFrom, LocalDateTime availableTo,
                           String doctorName, String specialty) {
}