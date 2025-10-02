package com.example.calendar.infrastructure.persistence.repository;

import com.example.calendar.domain.model.Booking;
import com.example.calendar.domain.model.BookingStatus;
import com.example.calendar.domain.repository.BookingRepository;
import com.example.calendar.infrastructure.persistence.entity.BookingEntity;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class BookingRepositoryAdapter implements BookingRepository {
    private final JpaBookingRepository bookingRepository;

    public BookingRepositoryAdapter(JpaBookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public Booking save(Booking booking) {
        BookingEntity saved = bookingRepository.save(BookingEntity.fromDomain(booking));
        return saved.toDomain();
    }

    @Override
    public Optional<Booking> findById(UUID id) {
        return bookingRepository.findById(id).map(BookingEntity::toDomain);
    }

    @Override
    public Optional<Booking> findByIdAndDeveloperId(UUID id, UUID developerId) {
        return bookingRepository.findByIdAndDeveloperId(id, developerId).map(BookingEntity::toDomain);
    }

    @Override
    public List<Booking> findAllByDeveloperIdAndStartAtBetweenOrderByStartAt(
            UUID developerId, OffsetDateTime from, OffsetDateTime to) {
        return bookingRepository
                .findAllByDeveloperIdAndStartAtBetweenOrderByStartAt(developerId, from, to)
                .stream()
                .map(BookingEntity::toDomain)
                .toList();
    }

    @Override
    public List<Booking> findAllByDeveloperIdAndStatusInAndStartAtBetween(
            UUID developerId, Collection<BookingStatus> statuses, OffsetDateTime from, OffsetDateTime to) {
        return bookingRepository
                .findAllByDeveloperIdAndStatusInAndStartAtBetween(developerId, statuses, from, to)
                .stream()
                .map(BookingEntity::toDomain)
                .toList();
    }

    @Override
    public List<Booking> findAllByStatusInAndStartAtBetween(
            Collection<BookingStatus> statuses, OffsetDateTime from, OffsetDateTime to) {
        return bookingRepository
                .findAllByStatusInAndStartAtBetween(statuses, from, to)
                .stream()
                .map(BookingEntity::toDomain)
                .toList();
    }
}
