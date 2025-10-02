package com.example.calendar.application.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record BulkAvailabilityRequest(LocalDate startDate, LocalDate endDate, LocalTime dailyStart, LocalTime dailyEnd) {}
