package com.example.calendar.dto;

import com.example.calendar.model.BookingStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        OffsetDateTime startAt,
        int durationMinutes,
        BookingStatus status,
        String eventTypeName,
        String company,
        String hrName,
        String hrEmail,
        String meetingLink,
        String createdByRole) {}
