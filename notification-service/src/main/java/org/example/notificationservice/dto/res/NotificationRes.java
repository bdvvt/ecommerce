package org.example.notificationservice.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRes {
    private String notificationId;
    private String eventId;
    private String channel;
    private String recipient;
    private String status;
    private String providerRef;
    private String subject;
    private String content;
    private Instant createdAt;
}
