package com.example.calendar.infrastructure.persistence.repository;

import com.example.calendar.domain.model.AvailabilitySlot;
import com.example.calendar.domain.repository.AvailabilitySlotRepository;
import com.example.calendar.infrastructure.persistence.entity.AvailabilitySlotEntity;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AvailabilitySlotRepositoryAdapter implements AvailabilitySlotRepository {
    private final JpaAvailabilitySlotRepository availabilitySlotRepository;

    public AvailabilitySlotRepositoryAdapter(JpaAvailabilitySlotRepository availabilitySlotRepository) {
        this.availabilitySlotRepository = availabilitySlotRepository;
    }

    @Override
    public List<AvailabilitySlot> findAllByDeveloperIdAndStartAtBetweenOrderByStartAt(
            UUID developerId, OffsetDateTime from, OffsetDateTime to) {
        return availabilitySlotRepository
                .findAllByDeveloperIdAndStartAtBetweenOrderByStartAt(developerId, from, to)
                .stream()
                .map(AvailabilitySlotEntity::toDomain)
                .toList();
    }

    @Override
    public List<AvailabilitySlot> lockRange(UUID developerId, OffsetDateTime from, OffsetDateTime to) {
        return availabilitySlotRepository
                .lockRange(developerId, from, to)
                .stream()
                .map(AvailabilitySlotEntity::toDomain)
                .toList();
    }

    @Override
    public boolean existsByDeveloperIdAndStartAt(UUID developerId, OffsetDateTime startAt) {
        return availabilitySlotRepository.existsByDeveloperIdAndStartAt(developerId, startAt);
    }

    @Override
    public Optional<AvailabilitySlot> findByDeveloperIdAndStartAt(UUID developerId, OffsetDateTime startAt) {
        return availabilitySlotRepository.findByDeveloperIdAndStartAt(developerId, startAt)
                .map(AvailabilitySlotEntity::toDomain);
    }

    @Override
    public AvailabilitySlot save(AvailabilitySlot slot) {
        AvailabilitySlotEntity saved = availabilitySlotRepository.save(AvailabilitySlotEntity.fromDomain(slot));
        return saved.toDomain();
    }

    @Override
    public void delete(AvailabilitySlot slot) {
        if (slot.getId() != null) {
            availabilitySlotRepository.deleteById(slot.getId());
        } else {
            availabilitySlotRepository.delete(AvailabilitySlotEntity.fromDomain(slot));
        }
    }

    @Override
    public void deleteAll(Iterable<AvailabilitySlot> slots) {
        List<UUID> ids = new ArrayList<>();
        List<AvailabilitySlotEntity> entities = new ArrayList<>();
        for (AvailabilitySlot slot : slots) {
            if (slot.getId() != null) {
                ids.add(slot.getId());
            } else {
                entities.add(AvailabilitySlotEntity.fromDomain(slot));
            }
        }
        if (!ids.isEmpty()) {
            availabilitySlotRepository.deleteAllById(ids);
        }
        if (!entities.isEmpty()) {
            availabilitySlotRepository.deleteAll(entities);
        }
    }
}
