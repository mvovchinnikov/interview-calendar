package com.example.calendar.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class AvailabilitySlot {
    private UUID id;
    private UUID developerId;
    private OffsetDateTime startAt;
    private int durationMinutes = 30;

    public AvailabilitySlot() {}

    public AvailabilitySlot(UUID id, UUID developerId, OffsetDateTime startAt, int durationMinutes) {
        this.id = id;
        this.developerId = developerId;
        this.startAt = startAt;
        this.durationMinutes = durationMinutes;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getDeveloperId() {
        return developerId;
    }

    public void setDeveloperId(UUID developerId) {
        this.developerId = developerId;
    }

    public OffsetDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(OffsetDateTime startAt) {
        this.startAt = startAt;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}
