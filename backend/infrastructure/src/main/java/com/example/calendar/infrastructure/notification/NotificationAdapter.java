package com.example.calendar.infrastructure.notification;

import com.example.calendar.application.port.out.NotificationPort;
import com.example.calendar.domain.model.Booking;
import com.example.calendar.domain.model.User;
import com.example.calendar.infrastructure.telegram.TelegramClient;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class NotificationAdapter implements NotificationPort {
    private static final Logger log = LoggerFactory.getLogger(NotificationAdapter.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z");

    private final JavaMailSender mailSender;
    private final TelegramClient telegramClient;

    public NotificationAdapter(JavaMailSender mailSender, TelegramClient telegramClient) {
        this.mailSender = mailSender;
        this.telegramClient = telegramClient;
    }

    @Override
    public void notifyBookingCreated(User developer, Booking booking) {
        String subject = "New booking request: " + booking.getCompany();
        String text = buildBookingText(developer, booking, "A new interview was requested.");
        sendEmail(subject, text, developer.getEmail(), booking.getHrEmail());
        sendTelegram(developer, text);
    }

    @Override
    public void sendReminder(User developer, Booking booking, Duration untilStart) {
        String subject = "Upcoming interview in " + untilStart.toHoursPart() + "h" + (untilStart.toMinutesPart() > 0 ? untilStart.toMinutesPart() + "m" : "");
        String text = buildBookingText(
                developer, booking, "Reminder: interview starts in " + formatDuration(untilStart) + ".");
        sendEmail(subject, text, developer.getEmail(), booking.getHrEmail());
        sendTelegram(developer, text);
    }

    private void sendEmail(String subject, String text, String... recipients) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipients);
        message.setSubject(subject);
        message.setText(text);
        try {
            mailSender.send(message);
        } catch (MailException ex) {
            log.warn("Failed to send email: {}", ex.getMessage());
        }
    }

    private void sendTelegram(User developer, String text) {
        if (StringUtils.hasText(developer.getTelegramChatId())) {
            telegramClient.sendMessage(developer.getTelegramChatId(), text);
        }
    }

    private String buildBookingText(User developer, Booking booking, String header) {
        return header
                + "\nDeveloper: "
                + developer.getDisplayName()
                + "\nCompany: "
                + booking.getCompany()
                + "\nHR: "
                + booking.getHrName()
                + " ("
                + booking.getHrEmail()
                + ")\nWhen: "
                + FORMATTER.format(booking.getStartAt())
                + "\nDuration: "
                + booking.getDurationMinutes()
                + " minutes"
                + (StringUtils.hasText(booking.getMeetingLink()) ? "\nMeeting link: " + booking.getMeetingLink() : "");
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        StringBuilder builder = new StringBuilder();
        if (hours > 0) {
            builder.append(hours).append("h");
        }
        if (minutes > 0) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(minutes).append("m");
        }
        if (builder.length() == 0) {
            builder.append(duration.toMinutes()).append("m");
        }
        return builder.toString();
    }
}
