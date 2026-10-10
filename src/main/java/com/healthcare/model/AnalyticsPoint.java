package com.healthcare.model;

/** Holds one labeled count and its normalized percentage for a dashboard chart. */
public record AnalyticsPoint(String label, long count, int percentage) {
}