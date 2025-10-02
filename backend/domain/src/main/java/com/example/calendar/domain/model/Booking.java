package com.example.calendar.domain.model;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public class Booking {
    private UUID id;
    private UUID developerId;
    private Role createdByRole;
    private String eventTypeName;
    private OffsetDateTime startAt;
    private int durationMinutes;
    private BookingStatus status = BookingStatus.NOT_APPROVED;
    private String company;
    private String hrName;
    private String hrEmail;
    private String meetingLink;
    private OffsetDateTime createdAt;

    public Booking() {}

    public Booking(
            UUID id,
            UUID developerId,
            Role createdByRole,
            String eventTypeName,
            OffsetDateTime startAt,
            int durationMinutes,
            BookingStatus status,
            String company,
            String hrName,
            String hrEmail,
            String meetingLink,
            OffsetDateTime createdAt) {
        this.id = id;
        this.developerId = developerId;
        this.createdByRole = createdByRole;
        this.eventTypeName = eventTypeName;
        this.startAt = startAt;
        this.durationMinutes = durationMinutes;
        this.status = status == null ? BookingStatus.NOT_APPROVED : status;
        this.company = company;
        this.hrName = hrName;
        this.hrEmail = hrEmail;
        this.meetingLink = meetingLink;
        this.createdAt = createdAt;
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
        this.createdByRole = Objects.requireNonNull(createdByRole, "createdByRole");
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
        this.status = Objects.requireNonNull(status, "status");
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

    public OffsetDateTime getEndAt() {
        return startAt.plusMinutes(durationMinutes);
    }

    public void approve() {
        this.status = BookingStatus.APPROVED;
    }

    public void unapprove() {
        this.status = BookingStatus.NOT_APPROVED;
    }

    public void decline() {
        this.status = BookingStatus.DECLINED;
    }
}
