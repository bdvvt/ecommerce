package org.example.notificationservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreated {
    private Long orderId;
    private String customerRef;
    private BigDecimal amount;
    private String currency;
    private Instant reservationExpiry;

    @Builder.Default
    private Instant occurredAt = Instant.now();
}
