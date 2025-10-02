package com.example.calendar.infrastructure.persistence.repository;

import com.example.calendar.infrastructure.persistence.entity.AvailabilitySlotEntity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaAvailabilitySlotRepository extends JpaRepository<AvailabilitySlotEntity, UUID> {
    List<AvailabilitySlotEntity> findAllByDeveloperIdAndStartAtBetweenOrderByStartAt(
            UUID developerId, OffsetDateTime from, OffsetDateTime to);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from AvailabilitySlotEntity s where s.developerId = :developerId and s.startAt >= :from and s.startAt < :to order by s.startAt")
    List<AvailabilitySlotEntity> lockRange(
            @Param("developerId") UUID developerId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to);

    boolean existsByDeveloperIdAndStartAt(UUID developerId, OffsetDateTime startAt);

    Optional<AvailabilitySlotEntity> findByDeveloperIdAndStartAt(UUID developerId, OffsetDateTime startAt);
}
