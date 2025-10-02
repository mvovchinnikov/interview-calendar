package com.example.calendar.domain.repository;

import com.example.calendar.domain.model.AvailabilitySlot;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AvailabilitySlotRepository {
    List<AvailabilitySlot> findAllByDeveloperIdAndStartAtBetweenOrderByStartAt(
            UUID developerId, OffsetDateTime from, OffsetDateTime to);

    List<AvailabilitySlot> lockRange(UUID developerId, OffsetDateTime from, OffsetDateTime to);

    boolean existsByDeveloperIdAndStartAt(UUID developerId, OffsetDateTime startAt);

    Optional<AvailabilitySlot> findByDeveloperIdAndStartAt(UUID developerId, OffsetDateTime startAt);

    AvailabilitySlot save(AvailabilitySlot slot);

    void delete(AvailabilitySlot slot);

    void deleteAll(Iterable<AvailabilitySlot> slots);
}
