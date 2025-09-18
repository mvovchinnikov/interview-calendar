package com.example.calendar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateEventTypeRequest(@NotBlank @Size(max = 18) String name) {}
