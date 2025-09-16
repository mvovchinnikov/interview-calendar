package com.example.calendar.repository;

import com.example.calendar.model.AvailabilitySlot;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, UUID> {
    List<AvailabilitySlot> findAllByDeveloperIdAndStartAtBetweenOrderByStartAt(
            UUID developerId, OffsetDateTime from, OffsetDateTime to);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from AvailabilitySlot s where s.developerId = :developerId and s.startAt >= :from and s.startAt < :to order by s.startAt")
    List<AvailabilitySlot> lockRange(
            @Param("developerId") UUID developerId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to);

    boolean existsByDeveloperIdAndStartAt(UUID developerId, OffsetDateTime startAt);

    Optional<AvailabilitySlot> findByDeveloperIdAndStartAt(UUID developerId, OffsetDateTime startAt);
}
