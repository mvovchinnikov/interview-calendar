package com.example.calendar.domain.repository;

import com.example.calendar.domain.model.Booking;
import com.example.calendar.domain.model.BookingStatus;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository {
    Booking save(Booking booking);

    Optional<Booking> findById(UUID id);

    Optional<Booking> findByIdAndDeveloperId(UUID id, UUID developerId);

    List<Booking> findAllByDeveloperIdAndStartAtBetweenOrderByStartAt(
            UUID developerId, OffsetDateTime from, OffsetDateTime to);

    List<Booking> findAllByDeveloperIdAndStatusInAndStartAtBetween(
            UUID developerId, Collection<BookingStatus> statuses, OffsetDateTime from, OffsetDateTime to);

    List<Booking> findAllByStatusInAndStartAtBetween(
            Collection<BookingStatus> statuses, OffsetDateTime from, OffsetDateTime to);
}
