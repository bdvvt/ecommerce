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
public class PaymentFailed {
    private Long paymentId;
    private Long orderId;
    private String failureCategory;
    private String failureReason;

    @Builder.Default
    private Instant occurredAt = Instant.now();
}
