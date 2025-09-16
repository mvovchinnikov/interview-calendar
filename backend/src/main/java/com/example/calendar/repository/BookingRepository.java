package com.example.calendar.repository;

import com.example.calendar.model.Booking;
import com.example.calendar.model.BookingStatus;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findAllByDeveloperIdAndStartAtBetweenOrderByStartAt(
            UUID developerId, OffsetDateTime from, OffsetDateTime to);

    Optional<Booking> findByIdAndDeveloperId(UUID id, UUID developerId);

    List<Booking> findAllByDeveloperIdAndStatusInAndStartAtBetween(
            UUID developerId, Collection<BookingStatus> statuses, OffsetDateTime from, OffsetDateTime to);

    List<Booking> findAllByStatusInAndStartAtBetween(
            Collection<BookingStatus> statuses, OffsetDateTime from, OffsetDateTime to);
}
