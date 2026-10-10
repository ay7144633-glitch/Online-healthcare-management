package com.healthcare.model;

import java.time.LocalDate;
import java.util.List;

/** Groups a calendar date with the appointments scheduled for that day. */
public record AppointmentDay(LocalDate date, List<Appointment> appointments) {
}
