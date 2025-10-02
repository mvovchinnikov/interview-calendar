package com.example.calendar.domain.model;

import java.util.UUID;

public class EventType {
    private UUID id;
    private UUID developerId;
    private String name;

    public EventType() {}

    public EventType(UUID id, UUID developerId, String name) {
        this.id = id;
        this.developerId = developerId;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
