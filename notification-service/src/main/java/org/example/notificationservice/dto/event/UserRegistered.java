package org.example.notificationservice.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRegistered {
    private String eventId;
    private String eventType;
    private Long userId;
    private String email;
    private String fullName;
    private String loginIdentifier;
    private String otp;

    @Builder.Default
    private Instant occurredAt = Instant.now();
}
