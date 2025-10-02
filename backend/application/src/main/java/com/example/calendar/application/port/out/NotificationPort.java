package com.example.calendar.application.port.out;

import com.example.calendar.domain.model.Booking;
import com.example.calendar.domain.model.User;
import java.time.Duration;

public interface NotificationPort {
    void notifyBookingCreated(User developer, Booking booking);

    void sendReminder(User developer, Booking booking, Duration untilStart);
}
