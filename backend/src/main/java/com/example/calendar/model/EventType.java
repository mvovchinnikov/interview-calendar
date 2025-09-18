package com.example.calendar.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "event_type")
public class EventType {
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
}
