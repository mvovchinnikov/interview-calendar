package com.example.calendar.interfaces.web.controller;

import com.example.calendar.application.availability.AvailabilityService;
import com.example.calendar.application.booking.BookingService;
import com.example.calendar.application.dto.AvailabilitySlotDto;
import com.example.calendar.application.dto.BookingResponse;
import com.example.calendar.application.dto.BulkAvailabilityRequest;
import com.example.calendar.application.dto.CreateEventTypeRequest;
import com.example.calendar.application.dto.EventTypeResponse;
import com.example.calendar.application.dto.SingleAvailabilityRequest;
import com.example.calendar.application.eventtype.EventTypeService;
import com.example.calendar.application.user.UserService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/dev")
@Validated
public class DeveloperCalendarController {
    private final AvailabilityService availabilityService;
    private final BookingService bookingService;
    private final EventTypeService eventTypeService;
    private final UserService userService;

    public DeveloperCalendarController(
            AvailabilityService availabilityService,
            BookingService bookingService,
            EventTypeService eventTypeService,
            UserService userService) {
        this.availabilityService = availabilityService;
        this.bookingService = bookingService;
        this.eventTypeService = eventTypeService;
        this.userService = userService;
    }

    @GetMapping("/{developerId}/event-types")
    public List<EventTypeResponse> eventTypes(
            @PathVariable UUID developerId, @RequestHeader("X-Dev-Id") String devHeader) {
        requireDeveloperHeader(developerId, devHeader);
        return eventTypeService.list(developerId);
    }

    @PostMapping("/{developerId}/event-types")
    public EventTypeResponse createEventType(
            @PathVariable UUID developerId,
            @RequestHeader("X-Dev-Id") String devHeader,
            @Validated @RequestBody CreateEventTypeRequest request) {
        requireDeveloperHeader(developerId, devHeader);
        return eventTypeService.create(developerId, request);
    }

    @PostMapping("/{developerId}/availability")
    public AvailabilitySlotDto addAvailability(
            @PathVariable UUID developerId,
            @RequestHeader("X-Dev-Id") String devHeader,
            @Validated @RequestBody SingleAvailabilityRequest request) {
        requireDeveloperHeader(developerId, devHeader);
        return availabilityService.add(developerId, request);
    }

    @GetMapping("/{developerId}/availability")
    public List<AvailabilitySlotDto> listAvailability(
            @PathVariable UUID developerId,
            @RequestHeader("X-Dev-Id") String devHeader,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        requireDeveloperHeader(developerId, devHeader);
        return availabilityService.list(developerId, from, to);
    }

    @DeleteMapping("/{developerId}/availability")
    public ResponseEntity<Void> deleteAvailability(
            @PathVariable UUID developerId,
            @RequestHeader("X-Dev-Id") String devHeader,
            @Validated @RequestBody SingleAvailabilityRequest request) {
        requireDeveloperHeader(developerId, devHeader);
        availabilityService.remove(developerId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{developerId}/availability/bulk")
    public List<AvailabilitySlotDto> bulkAddAvailability(
            @PathVariable UUID developerId,
            @RequestHeader("X-Dev-Id") String devHeader,
            @Validated @RequestBody BulkAvailabilityRequest request) {
        requireDeveloperHeader(developerId, devHeader);
        return availabilityService.bulkAdd(developerId, request);
    }

    @GetMapping("/{developerId}/bookings")
    public List<BookingResponse> developerBookings(
            @PathVariable UUID developerId,
            @RequestHeader("X-Dev-Id") String devHeader,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        requireDeveloperHeader(developerId, devHeader);
        return bookingService.getDeveloperBookings(developerId, from, to);
    }

    @PostMapping("/{developerId}/bookings/{bookingId}/approve")
    public BookingResponse approve(
            @PathVariable UUID developerId,
            @PathVariable UUID bookingId,
            @RequestHeader("X-Dev-Id") String devHeader) {
        requireDeveloperHeader(developerId, devHeader);
        return bookingService.approve(developerId, bookingId);
    }

    @PostMapping("/{developerId}/bookings/{bookingId}/unapprove")
    public BookingResponse unapprove(
            @PathVariable UUID developerId,
            @PathVariable UUID bookingId,
            @RequestHeader("X-Dev-Id") String devHeader) {
        requireDeveloperHeader(developerId, devHeader);
        return bookingService.unapprove(developerId, bookingId);
    }

    @PostMapping("/{developerId}/bookings/{bookingId}/decline")
    public BookingResponse decline(
            @PathVariable UUID developerId,
            @PathVariable UUID bookingId,
            @RequestHeader("X-Dev-Id") String devHeader) {
        requireDeveloperHeader(developerId, devHeader);
        return bookingService.decline(developerId, bookingId);
    }

    private void requireDeveloperHeader(UUID developerId, String header) {
        if (header == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Missing X-Dev-Id header");
        }
        UUID provided;
        try {
            provided = UUID.fromString(header);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid developer header");
        }
        if (!provided.equals(developerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Developer header mismatch");
        }
        userService.getDeveloper(developerId);
    }
}
