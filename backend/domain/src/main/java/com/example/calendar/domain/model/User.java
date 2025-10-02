package com.example.calendar.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class User {
    private UUID id;
    private Role role;
    private String displayName;
    private String email;
    private String telegramChatId;
    private String publicToken;
    private OffsetDateTime createdAt;

    public User() {}

    public User(
            UUID id,
            Role role,
            String displayName,
            String email,
            String telegramChatId,
            String publicToken,
            OffsetDateTime createdAt) {
        this.id = id;
        this.role = role;
        this.displayName = displayName;
        this.email = email;
        this.telegramChatId = telegramChatId;
        this.publicToken = publicToken;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelegramChatId() {
        return telegramChatId;
    }

    public void setTelegramChatId(String telegramChatId) {
        this.telegramChatId = telegramChatId;
    }

    public String getPublicToken() {
        return publicToken;
    }

    public void setPublicToken(String publicToken) {
        this.publicToken = publicToken;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isDeveloper() {
        return role == Role.DEV;
    }
}
