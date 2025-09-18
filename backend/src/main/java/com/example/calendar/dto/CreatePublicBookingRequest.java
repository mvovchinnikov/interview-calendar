package com.example.calendar.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record CreatePublicBookingRequest(
        @NotNull @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mmXXX") OffsetDateTime startAt,
        @Positive int durationMinutes,
        @NotBlank @Size(max = 18) String eventTypeName,
        @NotNull @Pattern(regexp = "HR1|HR2") String createdByRole,
        @NotBlank String company,
        @NotBlank String hrName,
        @Email @NotBlank String hrEmail,
        @Size(max = 255) String meetingLink) {}
