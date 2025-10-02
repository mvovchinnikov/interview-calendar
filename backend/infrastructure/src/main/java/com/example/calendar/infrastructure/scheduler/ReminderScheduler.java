package com.example.calendar.infrastructure.scheduler;

import com.example.calendar.application.port.out.NotificationPort;
import com.example.calendar.application.user.UserService;
import com.example.calendar.domain.model.Booking;
import com.example.calendar.domain.model.BookingStatus;
import com.example.calendar.domain.model.User;
import com.example.calendar.domain.repository.BookingRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReminderScheduler {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final NotificationPort notificationPort;
    private final Clock clock;
    private final Map<UUID, EnumSet<ReminderType>> dispatched = new ConcurrentHashMap<>();

    public ReminderScheduler(
            BookingRepository bookingRepository,
            UserService userService,
            NotificationPort notificationPort,
            Clock clock) {
        this.bookingRepository = bookingRepository;
        this.userService = userService;
        this.notificationPort = notificationPort;
        this.clock = clock;
    }

    @Scheduled(cron = "0 */15 * * * *")
    public void run() {
        OffsetDateTime now = OffsetDateTime.now(clock);
        cleanup(now);
        processReminder(now, Duration.ofHours(24), ReminderType.HOURS_24);
        processReminder(now, Duration.ofHours(1), ReminderType.HOUR_1);
    }

    private void processReminder(OffsetDateTime now, Duration target, ReminderType type) {
        OffsetDateTime windowStart = now.plus(target).minusMinutes(15);
        OffsetDateTime windowEnd = now.plus(target).plusMinutes(15);
        List<Booking> bookings = bookingRepository.findAllByStatusInAndStartAtBetween(
                EnumSet.of(BookingStatus.APPROVED), windowStart, windowEnd);
        for (Booking booking : bookings) {
            Duration until = Duration.between(now, booking.getStartAt());
            if (until.isNegative()) {
                continue;
            }
            EnumSet<ReminderType> sent = dispatched.computeIfAbsent(booking.getId(), key -> EnumSet.noneOf(ReminderType.class));
            if (sent.contains(type)) {
                continue;
            }
            User developer = userService.getDeveloper(booking.getDeveloperId());
            notificationPort.sendReminder(developer, booking, until);
            sent.add(type);
        }
    }

    private void cleanup(OffsetDateTime now) {
        dispatched.entrySet().removeIf(entry ->
                bookingRepository
                        .findById(entry.getKey())
                        .map(booking -> booking.getStartAt().isBefore(now.minusMinutes(30)))
                        .orElse(true));
    }

    private enum ReminderType {
        HOURS_24,
        HOUR_1
    }
}
