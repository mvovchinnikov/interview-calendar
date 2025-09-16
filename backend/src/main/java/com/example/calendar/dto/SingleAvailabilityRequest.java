package com.example.calendar.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record SingleAvailabilityRequest(@NotNull @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mmXXX") OffsetDateTime startAt) {}
