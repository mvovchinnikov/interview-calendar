package com.example.calendar.application.availability;

import com.example.calendar.application.dto.AvailabilitySlotDto;
import com.example.calendar.application.dto.BulkAvailabilityRequest;
import com.example.calendar.application.dto.SingleAvailabilityRequest;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface AvailabilityService {
    List<AvailabilitySlotDto> list(UUID developerId, OffsetDateTime from, OffsetDateTime to);

    AvailabilitySlotDto add(UUID developerId, SingleAvailabilityRequest request);

    void remove(UUID developerId, SingleAvailabilityRequest request);

    List<AvailabilitySlotDto> bulkAdd(UUID developerId, BulkAvailabilityRequest request);
}
