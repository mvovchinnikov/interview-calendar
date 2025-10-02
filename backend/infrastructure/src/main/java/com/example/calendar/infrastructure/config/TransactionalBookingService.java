package com.example.calendar.infrastructure.config;

import com.example.calendar.application.booking.DefaultBookingService;
import com.example.calendar.domain.repository.AvailabilitySlotRepository;
import com.example.calendar.domain.repository.BookingRepository;
import com.example.calendar.domain.repository.EventTypeRepository;
import com.example.calendar.application.port.out.NotificationPort;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TransactionalBookingService extends DefaultBookingService {
    public TransactionalBookingService(
            BookingRepository bookingRepository,
            AvailabilitySlotRepository availabilitySlotRepository,
            EventTypeRepository eventTypeRepository,
            NotificationPort notificationPort,
            Clock clock) {
        super(bookingRepository, availabilitySlotRepository, eventTypeRepository, notificationPort, clock);
    }
}
