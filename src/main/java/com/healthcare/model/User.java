package com.healthcare.model;

/** Holds account identity, role, specialty, and activation state for application workflows. */
public record User(long id, String fullName, String email, String role, String specialty, boolean active) {
}