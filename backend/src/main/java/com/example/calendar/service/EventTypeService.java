package com.example.calendar.service;

import com.example.calendar.dto.CreateEventTypeRequest;
import com.example.calendar.dto.EventTypeResponse;
import com.example.calendar.model.EventType;
import com.example.calendar.repository.EventTypeRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventTypeService {
    private final EventTypeRepository eventTypeRepository;

    public EventTypeService(EventTypeRepository eventTypeRepository) {
        this.eventTypeRepository = eventTypeRepository;
    }

    @Transactional(readOnly = true)
    public List<EventTypeResponse> list(UUID developerId) {
        return eventTypeRepository.findAllByDeveloperIdOrderByName(developerId).stream()
                .map(type -> new EventTypeResponse(type.getId(), type.getName()))
                .toList();
    }

    @Transactional
    public EventTypeResponse create(UUID developerId, CreateEventTypeRequest request) {
        String name = request.name().trim();
        eventTypeRepository.findByDeveloperIdAndNameIgnoreCase(developerId, name)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Event type already exists");
                });
        EventType type = new EventType();
        type.setDeveloperId(developerId);
        type.setName(name);
        EventType saved = eventTypeRepository.save(type);
        return new EventTypeResponse(saved.getId(), saved.getName());
    }
}
