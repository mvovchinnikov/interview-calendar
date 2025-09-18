package com.example.calendar.dto;

import java.util.List;

public record PublicAvailabilityResponse(List<AvailabilitySlotDto> slots) {}
