package com.example.calendar.application.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.calendar.application.dto.BookingResponse;
import com.example.calendar.application.dto.CreatePublicBookingRequest;
import com.example.calendar.application.dto.PublicBookingResponse;
import com.example.calendar.application.port.out.NotificationPort;
import com.example.calendar.domain.model.AvailabilitySlot;
import com.example.calendar.domain.model.Booking;
import com.example.calendar.domain.model.BookingStatus;
import com.example.calendar.domain.model.EventType;
import com.example.calendar.domain.model.Role;
import com.example.calendar.domain.model.User;
import com.example.calendar.domain.repository.AvailabilitySlotRepository;
import com.example.calendar.domain.repository.BookingRepository;
import com.example.calendar.domain.repository.EventTypeRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultBookingServiceTest {
    private static final OffsetDateTime NOW = OffsetDateTime.ofInstant(Instant.parse("2025-01-01T10:00:00Z"), ZoneOffset.UTC);

    @Mock private BookingRepository bookingRepository;
    @Mock private AvailabilitySlotRepository availabilitySlotRepository;
    @Mock private EventTypeRepository eventTypeRepository;
    @Mock private NotificationPort notificationPort;

    private DefaultBookingService bookingService;
    private User developer;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW.toInstant(), ZoneOffset.UTC);
        bookingService = new DefaultBookingService(
                bookingRepository, availabilitySlotRepository, eventTypeRepository, notificationPort, clock);
        developer = new User();
        developer.setId(UUID.randomUUID());
        developer.setDisplayName("Dev");
        developer.setEmail("dev@example.com");
    }

    @Test
    void createPublicBookingConsumesContiguousSlots() {
        CreatePublicBookingRequest request = new CreatePublicBookingRequest(
                NOW.plusHours(1), 60, "Screening", "HR", "Acme", "Alice", "alice@example.com", null);
        AvailabilitySlot first = slotAt(request.startAt());
        AvailabilitySlot second = slotAt(request.startAt().plusMinutes(30));
        when(eventTypeRepository.findByDeveloperIdAndNameIgnoreCase(developer.getId(), "Screening"))
                .thenReturn(Optional.of(new EventType()));
        when(availabilitySlotRepository.lockRange(developer.getId(), request.startAt(), request.startAt().plusMinutes(60)))
                .thenReturn(List.of(first, second));
        Booking saved = bookingFromRequest(request);
        saved.setId(UUID.randomUUID());
        when(bookingRepository.save(any(Booking.class))).thenReturn(saved);

        BookingResponse response = bookingService.createPublicBooking(developer, request);

        assertThat(response.id()).isEqualTo(saved.getId());
        ArgumentCaptor<Iterable<AvailabilitySlot>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(availabilitySlotRepository).deleteAll(captor.capture());
        assertThat(captor.getValue()).containsExactly(first, second);
        verify(notificationPort).notifyBookingCreated(developer, saved);
    }

    @Test
    void createPublicBookingFailsWhenSlotMissing() {
        CreatePublicBookingRequest request = new CreatePublicBookingRequest(
                NOW.plusHours(1), 60, "Screening", "HR", "Acme", "Alice", "alice@example.com", null);
        when(eventTypeRepository.findByDeveloperIdAndNameIgnoreCase(developer.getId(), "Screening"))
                .thenReturn(Optional.of(new EventType()));
        when(availabilitySlotRepository.lockRange(developer.getId(), request.startAt(), request.startAt().plusMinutes(60)))
                .thenReturn(List.of(slotAt(request.startAt())));

        assertThatThrownBy(() -> bookingService.createPublicBooking(developer, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no longer available");
    }

    @Test
    void createPublicBookingPropagatesDataIntegrityIssues() {
        CreatePublicBookingRequest request = new CreatePublicBookingRequest(
                NOW.plusHours(1), 30, "Screening", "HR", "Acme", "Alice", "alice@example.com", null);
        AvailabilitySlot slot = slotAt(request.startAt());
        when(eventTypeRepository.findByDeveloperIdAndNameIgnoreCase(developer.getId(), "Screening"))
                .thenReturn(Optional.of(new EventType()));
        when(availabilitySlotRepository.lockRange(developer.getId(), request.startAt(), request.startAt().plusMinutes(30)))
                .thenReturn(List.of(slot));
        when(bookingRepository.save(any())).thenReturn(bookingFromRequest(request));
        doThrow(new RuntimeException("conflict")).when(availabilitySlotRepository).deleteAll(any());

        assertThatThrownBy(() -> bookingService.createPublicBooking(developer, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("reserve availability");
    }

    @Test
    void createPublicBookingAcceptsLegacyHrAliases() {
        CreatePublicBookingRequest request = new CreatePublicBookingRequest(
                NOW.plusHours(1), 30, "Screening", "HR1", "Acme", "Alice", "alice@example.com", null);
        when(eventTypeRepository.findByDeveloperIdAndNameIgnoreCase(developer.getId(), "Screening"))
                .thenReturn(Optional.of(new EventType()));
        when(availabilitySlotRepository.lockRange(developer.getId(), request.startAt(), request.startAt().plusMinutes(30)))
                .thenReturn(List.of(slotAt(request.startAt())));
        when(bookingRepository.save(any())).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(UUID.randomUUID());
            return booking;
        });

        bookingService.createPublicBooking(developer, request);

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());
        assertThat(bookingCaptor.getValue().getCreatedByRole()).isEqualTo(Role.HR);
    }

    @Test
    void declineRestoresAvailabilityUnits() {
        UUID bookingId = UUID.randomUUID();
        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setDeveloperId(developer.getId());
        booking.setStartAt(NOW.plusHours(2));
        booking.setDurationMinutes(120);
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findByIdAndDeveloperId(bookingId, developer.getId()))
                .thenReturn(Optional.of(booking));
        when(availabilitySlotRepository.existsByDeveloperIdAndStartAt(any(), any())).thenReturn(false);

        bookingService.decline(developer.getId(), bookingId);

        verify(availabilitySlotRepository, times(4)).save(any(AvailabilitySlot.class));
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.DECLINED);
    }

    @Test
    void approvePreventsPastChanges() {
        UUID bookingId = UUID.randomUUID();
        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setDeveloperId(developer.getId());
        booking.setStartAt(NOW.minusMinutes(30));
        when(bookingRepository.findByIdAndDeveloperId(bookingId, developer.getId()))
                .thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.approve(developer.getId(), bookingId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("past");
    }

    @Test
    void publicBookingsHideDetailsForNonMatchingViewerRole() {
        Booking booking = new Booking();
        booking.setId(UUID.randomUUID());
        booking.setDeveloperId(developer.getId());
        booking.setStartAt(NOW.plusHours(1));
        booking.setDurationMinutes(60);
        booking.setStatus(BookingStatus.NOT_APPROVED);
        booking.setEventTypeName("Screening");
        booking.setCompany("Acme");
        booking.setHrName("Alice");
        booking.setHrEmail("alice@example.com");
        booking.setCreatedByRole(Role.HR);
        when(bookingRepository.findAllByDeveloperIdAndStartAtBetweenOrderByStartAt(developer.getId(), NOW, NOW.plusDays(1)))
                .thenReturn(List.of(booking));

        List<PublicBookingResponse> responses =
                bookingService.getPublicBookings(developer.getId(), NOW, NOW.plusDays(1), Role.DEV);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).occupied()).isTrue();
        assertThat(responses.get(0).company()).isNull();
    }

    private AvailabilitySlot slotAt(OffsetDateTime startAt) {
        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setId(UUID.randomUUID());
        slot.setDeveloperId(developer.getId());
        slot.setStartAt(startAt);
        slot.setDurationMinutes(30);
        return slot;
    }

    private Booking bookingFromRequest(CreatePublicBookingRequest request) {
        Booking booking = new Booking();
        booking.setDeveloperId(developer.getId());
        booking.setCreatedByRole(Role.valueOf(request.createdByRole()));
        booking.setStartAt(request.startAt());
        booking.setDurationMinutes(request.durationMinutes());
        booking.setCompany(request.company());
        booking.setHrName(request.hrName());
        booking.setHrEmail(request.hrEmail());
        booking.setEventTypeName(request.eventTypeName());
        return booking;
    }
}
