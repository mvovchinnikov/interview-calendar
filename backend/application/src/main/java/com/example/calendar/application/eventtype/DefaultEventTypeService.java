package com.example.calendar.application.eventtype;

import com.example.calendar.application.dto.CreateEventTypeRequest;
import com.example.calendar.application.dto.EventTypeResponse;
import com.example.calendar.domain.model.EventType;
import com.example.calendar.domain.repository.EventTypeRepository;
import java.util.List;
import java.util.UUID;

public class DefaultEventTypeService implements EventTypeService {
    private final EventTypeRepository eventTypeRepository;

    public DefaultEventTypeService(EventTypeRepository eventTypeRepository) {
        this.eventTypeRepository = eventTypeRepository;
    }

    @Override
    public List<EventTypeResponse> list(UUID developerId) {
        return eventTypeRepository.findAllByDeveloperIdOrderByName(developerId).stream()
                .map(type -> new EventTypeResponse(type.getId(), type.getName()))
                .toList();
    }

    @Override
    public EventTypeResponse create(UUID developerId, CreateEventTypeRequest request) {
        String name = requireNonBlank(request.name());
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

    private String requireNonBlank(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (value.length() > 18) {
            throw new IllegalArgumentException("name must not exceed 18 characters");
        }
        return value.trim();
    }
}
