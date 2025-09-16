package com.example.calendar.repository;

import com.example.calendar.model.EventType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventTypeRepository extends JpaRepository<EventType, UUID> {
    List<EventType> findAllByDeveloperIdOrderByName(UUID developerId);

    Optional<EventType> findByDeveloperIdAndNameIgnoreCase(UUID developerId, String name);
}
