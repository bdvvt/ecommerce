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
public class OrderConfirmed {
    private Long orderId;
    private String status;
    private Instant confirmedAt;

    @Builder.Default
    private Instant occurredAt = Instant.now();
}
