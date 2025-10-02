package com.example.calendar.application.dto;

import java.time.OffsetDateTime;

public record CreatePublicBookingRequest(
        OffsetDateTime startAt,
        int durationMinutes,
        String eventTypeName,
        String createdByRole,
        String company,
        String hrName,
        String hrEmail,
        String meetingLink) {}
