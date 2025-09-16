package com.example.calendar.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record BulkAvailabilityRequest(
        @NotNull @JsonFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
        @NotNull @JsonFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
        @NotNull @JsonFormat(pattern = "HH:mm") LocalTime dailyStart,
        @NotNull @JsonFormat(pattern = "HH:mm") LocalTime dailyEnd) {}
