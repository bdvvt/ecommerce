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
public class OrderCancelled {
    private Long orderId;
    private String reason;
    private Boolean refundRequired;

    @Builder.Default
    private Instant occurredAt = Instant.now();
}
