package org.example.notificationservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "notification_logs")
public class NotificationLog {

    @Id
    private String notificationId;

    private String eventId;

    private String channel;

    private String recipient; // recipient reference

    private String status;

    private String providerRef;

    private String subject;

    private String content;

    @CreatedDate
    private Instant createdAt;
}
