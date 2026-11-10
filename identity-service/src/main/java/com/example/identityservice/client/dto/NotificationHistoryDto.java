package com.example.identityservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationHistoryDto {
    private String id;
    private Long userId;
    private String channel;
    private String title;
    private String content;
    private String status;
    private LocalDateTime sentAt;
}
