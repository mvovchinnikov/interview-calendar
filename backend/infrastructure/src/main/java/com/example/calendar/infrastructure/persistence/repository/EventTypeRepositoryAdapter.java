package com.example.calendar.infrastructure.persistence.repository;

import com.example.calendar.domain.model.EventType;
import com.example.calendar.domain.repository.EventTypeRepository;
import com.example.calendar.infrastructure.persistence.entity.EventTypeEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class EventTypeRepositoryAdapter implements EventTypeRepository {
    private final JpaEventTypeRepository eventTypeRepository;

    public EventTypeRepositoryAdapter(JpaEventTypeRepository eventTypeRepository) {
        this.eventTypeRepository = eventTypeRepository;
    }

    @Override
    public List<EventType> findAllByDeveloperIdOrderByName(UUID developerId) {
        return eventTypeRepository.findAllByDeveloperIdOrderByName(developerId).stream()
                .map(EventTypeEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<EventType> findByDeveloperIdAndNameIgnoreCase(UUID developerId, String name) {
        return eventTypeRepository.findByDeveloperIdAndNameIgnoreCase(developerId, name)
                .map(EventTypeEntity::toDomain);
    }

    @Override
    public EventType save(EventType eventType) {
        EventTypeEntity saved = eventTypeRepository.save(EventTypeEntity.fromDomain(eventType));
        return saved.toDomain();
    }
}
