package com.example.calendar.application.eventtype;

import com.example.calendar.application.dto.CreateEventTypeRequest;
import com.example.calendar.application.dto.EventTypeResponse;
import java.util.List;
import java.util.UUID;

public interface EventTypeService {
    List<EventTypeResponse> list(UUID developerId);

    EventTypeResponse create(UUID developerId, CreateEventTypeRequest request);
}
