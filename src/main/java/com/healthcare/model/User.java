package com.healthcare.model;

public record User(long id, String fullName, String email, String role, String specialty, boolean active) {
}