package com.example.calendar.domain.repository;

import com.example.calendar.domain.model.EventType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventTypeRepository {
    List<EventType> findAllByDeveloperIdOrderByName(UUID developerId);

    Optional<EventType> findByDeveloperIdAndNameIgnoreCase(UUID developerId, String name);

    EventType save(EventType eventType);
}
