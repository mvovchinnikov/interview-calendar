package com.example.calendar.application.dto;

import java.util.List;

public record PublicAvailabilityResponse(List<AvailabilitySlotDto> slots) {}
