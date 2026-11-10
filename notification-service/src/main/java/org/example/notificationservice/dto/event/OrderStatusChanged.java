package org.example.notificationservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusChanged {
    private Long orderId;
    private String oldStatus;
    private String newStatus;

    @Builder.Default
    private Instant occurredAt = Instant.now();
}
