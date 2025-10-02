package com.example.calendar.interfaces.web.controller;

import com.example.calendar.application.availability.AvailabilityService;
import com.example.calendar.application.booking.BookingService;
import com.example.calendar.application.dto.CreatePublicBookingRequest;
import com.example.calendar.application.dto.EventTypeResponse;
import com.example.calendar.application.dto.PublicAvailabilityResponse;
import com.example.calendar.application.dto.PublicBookingResponse;
import com.example.calendar.application.eventtype.EventTypeService;
import com.example.calendar.application.user.UserService;
import com.example.calendar.domain.model.Role;
import com.example.calendar.domain.model.User;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
@Validated
public class PublicCalendarController {
    private final UserService userService;
    private final AvailabilityService availabilityService;
    private final BookingService bookingService;
    private final EventTypeService eventTypeService;

    public PublicCalendarController(
            UserService userService,
            AvailabilityService availabilityService,
            BookingService bookingService,
            EventTypeService eventTypeService) {
        this.userService = userService;
        this.availabilityService = availabilityService;
        this.bookingService = bookingService;
        this.eventTypeService = eventTypeService;
    }

    @GetMapping("/{token}/availability")
    public PublicAvailabilityResponse availability(
            @PathVariable String token,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        User developer = userService.getDeveloperByToken(token);
        return new PublicAvailabilityResponse(availabilityService.list(developer.getId(), from, to));
    }

    @GetMapping("/{token}/bookings")
    public List<PublicBookingResponse> bookings(
            @PathVariable String token,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(value = "asRole", required = false) String asRole) {
        User developer = userService.getDeveloperByToken(token);
        Role viewer = parseViewerRole(asRole);
        return bookingService.getPublicBookings(developer.getId(), from, to, viewer);
    }

    @PostMapping("/{token}/bookings")
    public ResponseEntity<?> createBooking(
            @PathVariable String token, @Validated @RequestBody CreatePublicBookingRequest request) {
        User developer = userService.getDeveloperByToken(token);
        return ResponseEntity.ok(bookingService.createPublicBooking(developer, request));
    }

    @GetMapping("/{token}/event-types")
    public List<EventTypeResponse> publicEventTypes(@PathVariable String token) {
        User developer = userService.getDeveloperByToken(token);
        return eventTypeService.list(developer.getId());
    }

    private Role parseViewerRole(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String upper = trimmed.toUpperCase(Locale.ROOT);
        try {
            return Role.valueOf(upper);
        } catch (IllegalArgumentException ex) {
            if (upper.startsWith("HR")) {
                return Role.HR;
            }
            throw ex;
        }
    }
}
