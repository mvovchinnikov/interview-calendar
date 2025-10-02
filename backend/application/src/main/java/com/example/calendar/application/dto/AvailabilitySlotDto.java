package com.example.calendar.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AvailabilitySlotDto(UUID id, OffsetDateTime startAt, int durationMinutes) {}
