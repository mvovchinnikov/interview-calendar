package com.example.calendar.infrastructure.persistence.entity;

import com.example.calendar.domain.model.EventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "event_type")
public class EventTypeEntity {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "developer_id", nullable = false, columnDefinition = "uuid")
    private UUID developerId;

    @Column(nullable = false, length = 18)
    private String name;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EventType toDomain() {
        EventType eventType = new EventType();
        eventType.setId(id);
        eventType.setDeveloperId(developerId);
        eventType.setName(name);
        return eventType;
    }

    public static EventTypeEntity fromDomain(EventType eventType) {
        EventTypeEntity entity = new EventTypeEntity();
        entity.setId(eventType.getId());
        entity.setDeveloperId(eventType.getDeveloperId());
        entity.setName(eventType.getName());
        return entity;
    }
}
