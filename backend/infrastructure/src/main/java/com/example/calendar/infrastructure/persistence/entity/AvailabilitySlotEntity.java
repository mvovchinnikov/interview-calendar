package com.example.calendar.infrastructure.persistence.entity;

import com.example.calendar.domain.model.AvailabilitySlot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "availability_slot")
public class AvailabilitySlotEntity {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "developer_id", nullable = false, columnDefinition = "uuid")
    private UUID developerId;

    @Column(name = "start_at", nullable = false)
    private OffsetDateTime startAt;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes = 30;

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

    public AvailabilitySlot toDomain() {
        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setId(id);
        slot.setDeveloperId(developerId);
        slot.setStartAt(startAt);
        slot.setDurationMinutes(durationMinutes);
        return slot;
    }

    public static AvailabilitySlotEntity fromDomain(AvailabilitySlot slot) {
        AvailabilitySlotEntity entity = new AvailabilitySlotEntity();
        entity.setId(slot.getId());
        entity.setDeveloperId(slot.getDeveloperId());
        entity.setStartAt(slot.getStartAt());
        entity.setDurationMinutes(slot.getDurationMinutes());
        return entity;
    }
}
