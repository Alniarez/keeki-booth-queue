package de.alniarez.config;

import java.time.LocalTime;

public record TimeSlot(LocalTime startTime, LocalTime endTime) {
}
