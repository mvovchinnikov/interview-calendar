package com.example.calendar.infrastructure.persistence.repository;

import com.example.calendar.infrastructure.persistence.entity.EventTypeEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaEventTypeRepository extends JpaRepository<EventTypeEntity, UUID> {
    List<EventTypeEntity> findAllByDeveloperIdOrderByName(UUID developerId);

    Optional<EventTypeEntity> findByDeveloperIdAndNameIgnoreCase(UUID developerId, String name);
}
