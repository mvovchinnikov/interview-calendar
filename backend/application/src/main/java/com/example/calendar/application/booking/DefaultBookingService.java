package com.example.calendar.application.booking;

import com.example.calendar.application.dto.BookingResponse;
import com.example.calendar.application.dto.CreatePublicBookingRequest;
import com.example.calendar.application.dto.PublicBookingResponse;
import com.example.calendar.application.port.out.NotificationPort;
import com.example.calendar.domain.model.AvailabilitySlot;
import com.example.calendar.domain.model.Booking;
import com.example.calendar.domain.model.BookingStatus;
import com.example.calendar.domain.model.Role;
import com.example.calendar.domain.model.User;
import com.example.calendar.domain.repository.AvailabilitySlotRepository;
import com.example.calendar.domain.repository.BookingRepository;
import com.example.calendar.domain.repository.EventTypeRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DefaultBookingService implements BookingService {
    private static final Set<Integer> ALLOWED_DURATIONS = Set.of(30, 60, 90, 120);

    private final BookingRepository bookingRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final EventTypeRepository eventTypeRepository;
    private final NotificationPort notificationPort;
    private final Clock clock;

    public DefaultBookingService(
            BookingRepository bookingRepository,
            AvailabilitySlotRepository availabilitySlotRepository,
            EventTypeRepository eventTypeRepository,
            NotificationPort notificationPort,
            Clock clock) {
        this.bookingRepository = bookingRepository;
        this.availabilitySlotRepository = availabilitySlotRepository;
        this.eventTypeRepository = eventTypeRepository;
        this.notificationPort = notificationPort;
        this.clock = clock;
    }

    @Override
    public BookingResponse createPublicBooking(User developer, CreatePublicBookingRequest request) {
        Role createdByRole = requireRole(request.createdByRole());
        OffsetDateTime startAt = requireNonNull(request.startAt(), "startAt");
        int duration = request.durationMinutes();
        validateStart(startAt);
        validateDuration(duration);
        ensureFuture(startAt);
        ensureEventTypeExists(developer.getId(), request.eventTypeName());
        OffsetDateTime endAt = startAt.plusMinutes(duration);
        List<AvailabilitySlot> slots = availabilitySlotRepository.lockRange(developer.getId(), startAt, endAt);
        Map<OffsetDateTime, AvailabilitySlot> byStart = new HashMap<>();
        for (AvailabilitySlot slot : slots) {
            byStart.put(slot.getStartAt(), slot);
        }
        List<AvailabilitySlot> toConsume = new ArrayList<>();
        for (OffsetDateTime cursor = startAt; cursor.isBefore(endAt); cursor = cursor.plusMinutes(30)) {
            AvailabilitySlot slot = byStart.get(cursor);
            if (slot == null) {
                throw new IllegalStateException("Requested time is no longer available");
            }
            toConsume.add(slot);
        }
        try {
            availabilitySlotRepository.deleteAll(toConsume);
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Failed to reserve availability", ex);
        }
        Booking booking = new Booking();
        booking.setDeveloperId(developer.getId());
        booking.setCreatedByRole(createdByRole);
        booking.setEventTypeName(request.eventTypeName().trim());
        booking.setStartAt(startAt);
        booking.setDurationMinutes(duration);
        booking.setStatus(BookingStatus.NOT_APPROVED);
        booking.setCompany(request.company());
        booking.setHrName(request.hrName());
        booking.setHrEmail(request.hrEmail());
        booking.setMeetingLink(request.meetingLink());
        Booking saved = bookingRepository.save(booking);
        notificationPort.notifyBookingCreated(developer, saved);
        return toBookingResponse(saved);
    }

    @Override
    public List<PublicBookingResponse> getPublicBookings(UUID developerId, OffsetDateTime from, OffsetDateTime to, Role viewer) {
        return bookingRepository
                .findAllByDeveloperIdAndStartAtBetweenOrderByStartAt(developerId, from, to)
                .stream()
                .map(booking -> toPublicBookingResponse(booking, viewer))
                .toList();
    }

    @Override
    public List<BookingResponse> getDeveloperBookings(UUID developerId, OffsetDateTime from, OffsetDateTime to) {
        return bookingRepository
                .findAllByDeveloperIdAndStartAtBetweenOrderByStartAt(developerId, from, to)
                .stream()
                .map(this::toBookingResponse)
                .toList();
    }

    @Override
    public BookingResponse approve(UUID developerId, UUID bookingId) {
        Booking booking = getBooking(developerId, bookingId);
        ensureFuture(booking.getStartAt());
        booking.approve();
        Booking saved = bookingRepository.save(booking);
        return toBookingResponse(saved);
    }

    @Override
    public BookingResponse unapprove(UUID developerId, UUID bookingId) {
        Booking booking = getBooking(developerId, bookingId);
        ensureFuture(booking.getStartAt());
        booking.unapprove();
        Booking saved = bookingRepository.save(booking);
        return toBookingResponse(saved);
    }

    @Override
    public BookingResponse decline(UUID developerId, UUID bookingId) {
        Booking booking = getBooking(developerId, bookingId);
        ensureFuture(booking.getStartAt());
        if (booking.getStatus() == BookingStatus.DECLINED) {
            return toBookingResponse(booking);
        }
        OffsetDateTime cursor = booking.getStartAt();
        for (int minutes = 0; minutes < booking.getDurationMinutes(); minutes += 30) {
            OffsetDateTime slotStart = cursor.plusMinutes(minutes);
            if (!availabilitySlotRepository.existsByDeveloperIdAndStartAt(developerId, slotStart)) {
                AvailabilitySlot slot = new AvailabilitySlot();
                slot.setDeveloperId(developerId);
                slot.setStartAt(slotStart);
                slot.setDurationMinutes(30);
                availabilitySlotRepository.save(slot);
            }
        }
        booking.decline();
        Booking saved = bookingRepository.save(booking);
        return toBookingResponse(saved);
    }

    @Override
    public List<Booking> findApprovedStartingWithin(UUID developerId, OffsetDateTime from, OffsetDateTime to) {
        return bookingRepository.findAllByDeveloperIdAndStatusInAndStartAtBetween(
                developerId, EnumSet.of(BookingStatus.APPROVED), from, to);
    }

    private void ensureEventTypeExists(UUID developerId, String eventTypeName) {
        String trimmed = requireNonNull(eventTypeName, "eventTypeName").trim();
        eventTypeRepository
                .findByDeveloperIdAndNameIgnoreCase(developerId, trimmed)
                .orElseThrow(() -> new IllegalArgumentException("Unknown event type"));
    }

    private void validateStart(OffsetDateTime startAt) {
        int minute = startAt.getMinute();
        if (minute != 0 && minute != 30) {
            throw new IllegalArgumentException("Start time must align to 30-minute increments");
        }
        if (startAt.getSecond() != 0 || startAt.getNano() != 0) {
            throw new IllegalArgumentException("Start time must not contain seconds");
        }
    }

    private void validateDuration(int duration) {
        if (!ALLOWED_DURATIONS.contains(duration)) {
            throw new IllegalArgumentException("Duration must be one of 30/60/90/120");
        }
    }

    private void ensureFuture(OffsetDateTime time) {
        if (time.isBefore(OffsetDateTime.now(clock))) {
            throw new IllegalArgumentException("Action is not allowed in the past");
        }
    }

    private Booking getBooking(UUID developerId, UUID bookingId) {
        return bookingRepository
                .findByIdAndDeveloperId(bookingId, developerId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
    }

    private BookingResponse toBookingResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getStartAt(),
                booking.getDurationMinutes(),
                booking.getStatus(),
                booking.getEventTypeName(),
                booking.getCompany(),
                booking.getHrName(),
                booking.getHrEmail(),
                booking.getMeetingLink(),
                booking.getCreatedByRole().name());
    }

    private PublicBookingResponse toPublicBookingResponse(Booking booking, Role viewer) {
        boolean canView = viewer != null && booking.getCreatedByRole() == viewer;
        return new PublicBookingResponse(
                booking.getId(),
                booking.getStartAt(),
                booking.getDurationMinutes(),
                booking.getStatus(),
                canView ? booking.getEventTypeName() : null,
                canView ? booking.getCompany() : null,
                canView ? booking.getHrName() : null,
                canView ? booking.getHrEmail() : null,
                canView ? booking.getMeetingLink() : null,
                booking.getCreatedByRole().name(),
                !canView);
    }

    private Role requireRole(String roleValue) {
        Role role = parseRole(roleValue);
        if (role == null) {
            throw new IllegalArgumentException("Unknown role: " + roleValue);
        }
        return role;
    }

    private Role parseRole(String roleValue) {
        if (roleValue == null) {
            return null;
        }
        String normalized = roleValue.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        String upper = normalized.toUpperCase(Locale.ROOT);
        try {
            return Role.valueOf(upper);
        } catch (IllegalArgumentException ex) {
            if (upper.startsWith("HR")) {
                return Role.HR;
            }
            return null;
        }
    }

    private OffsetDateTime requireNonNull(OffsetDateTime value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " must not be null");
        }
        return value;
    }

    private String requireNonNull(String value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " must not be null");
        }
        return value;
    }
}
