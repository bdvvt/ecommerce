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
public class RefundCompleted {
    private Long refundId;
    private Long paymentId;
    private Long orderId;
    private BigDecimal amount;

    @Builder.Default
    private Instant occurredAt = Instant.now();
}
