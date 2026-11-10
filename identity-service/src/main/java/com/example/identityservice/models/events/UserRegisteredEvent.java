package com.example.identityservice.models.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent implements Serializable {
    private String eventId;
    private String eventType;         // e.g. "USER_REGISTERED", "FORGOT_PASSWORD", "RESEND_OTP"
    private Long userId;
    private String email;
    private String fullName;
    private String loginIdentifier;
    private String otp;
    private String timestamp;
}
