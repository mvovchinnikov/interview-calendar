package com.example.calendar.infrastructure.persistence.entity;

import com.example.calendar.domain.model.Booking;
import com.example.calendar.domain.model.BookingStatus;
import com.example.calendar.domain.model.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "booking")
public class BookingEntity {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "developer_id", nullable = false, columnDefinition = "uuid")
    private UUID developerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "created_by_role", nullable = false)
    private Role createdByRole;

    @Column(name = "event_type_name", nullable = false, length = 18)
    private String eventTypeName;

    @Column(name = "start_at", nullable = false)
    private OffsetDateTime startAt;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.NOT_APPROVED;

    @Column(nullable = false)
    private String company;

    @Column(name = "hr_name", nullable = false)
    private String hrName;

    @Column(name = "hr_email", nullable = false)
    private String hrEmail;

    @Column(name = "meeting_link")
    private String meetingLink;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getDeveloperId() {
        return developerId;
    }

    public void setDeveloperId(UUID developerId) {
        this.developerId = developerId;
    }

    public Role getCreatedByRole() {
        return createdByRole;
    }

    public void setCreatedByRole(Role createdByRole) {
        this.createdByRole = createdByRole;
    }

    public String getEventTypeName() {
        return eventTypeName;
    }

    public void setEventTypeName(String eventTypeName) {
        this.eventTypeName = eventTypeName;
    }

    public OffsetDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(OffsetDateTime startAt) {
        this.startAt = startAt;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getHrName() {
        return hrName;
    }

    public void setHrName(String hrName) {
        this.hrName = hrName;
    }

    public String getHrEmail() {
        return hrEmail;
    }

    public void setHrEmail(String hrEmail) {
        this.hrEmail = hrEmail;
    }

    public String getMeetingLink() {
        return meetingLink;
    }

    public void setMeetingLink(String meetingLink) {
        this.meetingLink = meetingLink;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Booking toDomain() {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setDeveloperId(developerId);
        booking.setCreatedByRole(createdByRole);
        booking.setEventTypeName(eventTypeName);
        booking.setStartAt(startAt);
        booking.setDurationMinutes(durationMinutes);
        booking.setStatus(status);
        booking.setCompany(company);
        booking.setHrName(hrName);
        booking.setHrEmail(hrEmail);
        booking.setMeetingLink(meetingLink);
        booking.setCreatedAt(createdAt);
        return booking;
    }

    public static BookingEntity fromDomain(Booking booking) {
        BookingEntity entity = new BookingEntity();
        entity.setId(booking.getId());
        entity.setDeveloperId(booking.getDeveloperId());
        entity.setCreatedByRole(booking.getCreatedByRole());
        entity.setEventTypeName(booking.getEventTypeName());
        entity.setStartAt(booking.getStartAt());
        entity.setDurationMinutes(booking.getDurationMinutes());
        entity.setStatus(booking.getStatus());
        entity.setCompany(booking.getCompany());
        entity.setHrName(booking.getHrName());
        entity.setHrEmail(booking.getHrEmail());
        entity.setMeetingLink(booking.getMeetingLink());
        entity.setCreatedAt(booking.getCreatedAt());
        return entity;
    }
}
