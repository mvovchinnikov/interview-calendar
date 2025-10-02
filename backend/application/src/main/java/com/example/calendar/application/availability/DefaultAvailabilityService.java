package com.example.calendar.application.availability;

import com.example.calendar.application.dto.AvailabilitySlotDto;
import com.example.calendar.application.dto.BulkAvailabilityRequest;
import com.example.calendar.application.dto.SingleAvailabilityRequest;
import com.example.calendar.domain.model.AvailabilitySlot;
import com.example.calendar.domain.repository.AvailabilitySlotRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DefaultAvailabilityService implements AvailabilityService {
    private static final int SLOT_MINUTES = 30;

    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final Clock clock;

    public DefaultAvailabilityService(AvailabilitySlotRepository availabilitySlotRepository, Clock clock) {
        this.availabilitySlotRepository = availabilitySlotRepository;
        this.clock = clock;
    }

    @Override
    public List<AvailabilitySlotDto> list(UUID developerId, OffsetDateTime from, OffsetDateTime to) {
        return availabilitySlotRepository
                .findAllByDeveloperIdAndStartAtBetweenOrderByStartAt(developerId, from, to)
                .stream()
                .map(slot -> new AvailabilitySlotDto(slot.getId(), slot.getStartAt(), slot.getDurationMinutes()))
                .toList();
    }

    @Override
    public AvailabilitySlotDto add(UUID developerId, SingleAvailabilityRequest request) {
        OffsetDateTime startAt = requireNonNull(request.startAt(), "startAt");
        validateStart(startAt);
        ensureFuture(startAt);
        if (availabilitySlotRepository.existsByDeveloperIdAndStartAt(developerId, startAt)) {
            throw new IllegalStateException("Availability already exists");
        }
        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setDeveloperId(developerId);
        slot.setStartAt(startAt);
        slot.setDurationMinutes(SLOT_MINUTES);
        AvailabilitySlot saved = availabilitySlotRepository.save(slot);
        return new AvailabilitySlotDto(saved.getId(), saved.getStartAt(), saved.getDurationMinutes());
    }

    @Override
    public void remove(UUID developerId, SingleAvailabilityRequest request) {
        OffsetDateTime startAt = requireNonNull(request.startAt(), "startAt");
        ensureFuture(startAt);
        availabilitySlotRepository
                .findByDeveloperIdAndStartAt(developerId, startAt)
                .ifPresent(availabilitySlotRepository::delete);
    }

    @Override
    public List<AvailabilitySlotDto> bulkAdd(UUID developerId, BulkAvailabilityRequest request) {
        requireNonNull(request.startDate(), "startDate");
        requireNonNull(request.endDate(), "endDate");
        requireNonNull(request.dailyStart(), "dailyStart");
        requireNonNull(request.dailyEnd(), "dailyEnd");
        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("End date must be on or after start date");
        }
        if (!request.dailyEnd().isAfter(request.dailyStart())) {
            throw new IllegalArgumentException("Daily end time must be after start time");
        }
        List<AvailabilitySlotDto> created = new ArrayList<>();
        Set<OffsetDateTime> toCreate = new HashSet<>();
        LocalDate day = request.startDate();
        while (!day.isAfter(request.endDate())) {
            OffsetDateTime dayStart = day.atTime(request.dailyStart()).atOffset(ZoneOffset.UTC);
            OffsetDateTime dayEnd = day.atTime(request.dailyEnd()).atOffset(ZoneOffset.UTC);
            for (OffsetDateTime cursor = dayStart;
                    !cursor.isAfter(dayEnd.minusMinutes(SLOT_MINUTES));
                    cursor = cursor.plusMinutes(SLOT_MINUTES)) {
                if (cursor.isBefore(now())) {
                    continue;
                }
                if (!availabilitySlotRepository.existsByDeveloperIdAndStartAt(developerId, cursor)) {
                    toCreate.add(cursor);
                }
            }
            day = day.plusDays(1);
        }
        toCreate.stream().sorted().forEach(start -> {
            validateStart(start);
            AvailabilitySlot slot = new AvailabilitySlot();
            slot.setDeveloperId(developerId);
            slot.setStartAt(start);
            slot.setDurationMinutes(SLOT_MINUTES);
            try {
                AvailabilitySlot saved = availabilitySlotRepository.save(slot);
                created.add(new AvailabilitySlotDto(saved.getId(), saved.getStartAt(), saved.getDurationMinutes()));
            } catch (RuntimeException ex) {
                // another request may have created the slot, ignore
            }
        });
        return created;
    }

    private void ensureFuture(OffsetDateTime startAt) {
        if (startAt.isBefore(now())) {
            throw new IllegalArgumentException("Cannot modify past availability");
        }
    }

    private void validateStart(OffsetDateTime startAt) {
        int minute = startAt.getMinute();
        if (minute != 0 && minute != SLOT_MINUTES) {
            throw new IllegalArgumentException("Start time must align to 30-minute increments");
        }
        if (startAt.getSecond() != 0 || startAt.getNano() != 0) {
            throw new IllegalArgumentException("Start time must not contain seconds");
        }
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(clock);
    }

    private <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " must not be null");
        }
        return value;
    }
}
