package com.example.calendar.infrastructure.persistence.repository;

import com.example.calendar.infrastructure.persistence.entity.BookingEntity;
import com.example.calendar.domain.model.BookingStatus;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaBookingRepository extends JpaRepository<BookingEntity, UUID> {
    List<BookingEntity> findAllByDeveloperIdAndStartAtBetweenOrderByStartAt(
            UUID developerId, OffsetDateTime from, OffsetDateTime to);

    Optional<BookingEntity> findByIdAndDeveloperId(UUID id, UUID developerId);

    List<BookingEntity> findAllByDeveloperIdAndStatusInAndStartAtBetween(
            UUID developerId, Collection<BookingStatus> statuses, OffsetDateTime from, OffsetDateTime to);

    List<BookingEntity> findAllByStatusInAndStartAtBetween(
            Collection<BookingStatus> statuses, OffsetDateTime from, OffsetDateTime to);
}
