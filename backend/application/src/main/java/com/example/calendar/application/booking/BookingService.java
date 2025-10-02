package com.example.calendar.application.booking;

import com.example.calendar.application.dto.BookingResponse;
import com.example.calendar.application.dto.CreatePublicBookingRequest;
import com.example.calendar.application.dto.PublicBookingResponse;
import com.example.calendar.domain.model.Booking;
import com.example.calendar.domain.model.Role;
import com.example.calendar.domain.model.User;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface BookingService {
    BookingResponse createPublicBooking(User developer, CreatePublicBookingRequest request);

    List<PublicBookingResponse> getPublicBookings(UUID developerId, OffsetDateTime from, OffsetDateTime to, Role viewer);

    List<BookingResponse> getDeveloperBookings(UUID developerId, OffsetDateTime from, OffsetDateTime to);

    BookingResponse approve(UUID developerId, UUID bookingId);

    BookingResponse unapprove(UUID developerId, UUID bookingId);

    BookingResponse decline(UUID developerId, UUID bookingId);

    List<Booking> findApprovedStartingWithin(UUID developerId, OffsetDateTime from, OffsetDateTime to);
}
