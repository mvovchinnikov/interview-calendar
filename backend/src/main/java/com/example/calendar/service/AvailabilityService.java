package com.example.calendar.service;

import com.example.calendar.dto.AvailabilitySlotDto;
import com.example.calendar.dto.BulkAvailabilityRequest;
import com.example.calendar.dto.SingleAvailabilityRequest;
import com.example.calendar.model.AvailabilitySlot;
import com.example.calendar.repository.AvailabilitySlotRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
public class AvailabilityService {
    private static final int SLOT_MINUTES = 30;

    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final Clock clock;

    public AvailabilityService(AvailabilitySlotRepository availabilitySlotRepository, Clock clock) {
        this.availabilitySlotRepository = availabilitySlotRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<AvailabilitySlotDto> list(UUID developerId, OffsetDateTime from, OffsetDateTime to) {
        return availabilitySlotRepository
                .findAllByDeveloperIdAndStartAtBetweenOrderByStartAt(developerId, from, to)
                .stream()
                .map(slot -> new AvailabilitySlotDto(slot.getId(), slot.getStartAt(), slot.getDurationMinutes()))
                .toList();
    }

    @Transactional
    public AvailabilitySlotDto add(UUID developerId, SingleAvailabilityRequest request) {
        OffsetDateTime startAt = request.startAt();
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

    @Transactional
    public void remove(UUID developerId, SingleAvailabilityRequest request) {
        OffsetDateTime startAt = request.startAt();
        ensureFuture(startAt);
        availabilitySlotRepository
                .findByDeveloperIdAndStartAt(developerId, startAt)
                .ifPresent(availabilitySlotRepository::delete);
    }

    @Transactional
    public List<AvailabilitySlotDto> bulkAdd(UUID developerId, BulkAvailabilityRequest request) {
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
            } catch (DataIntegrityViolationException ex) {
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
        Assert.notNull(startAt, "startAt must not be null");
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
}
