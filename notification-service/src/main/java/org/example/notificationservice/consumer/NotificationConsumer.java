package org.example.notificationservice.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notificationservice.dto.event.*;
import org.example.notificationservice.dto.mapper.NotificationMapper;
import org.example.notificationservice.dto.req.SendNotificationReq;
import org.example.notificationservice.service.IMailService;
import org.example.notificationservice.service.INotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final IMailService mailService;
    private final INotificationService notificationService;
    private final NotificationMapper notificationMapper;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order-created", groupId = "notify-group")
    public void listenerOrderCreated(String payload) {
        log.info("Nhận sự kiện [order-created]: {}", payload);
        try {
            if (!payload.trim().startsWith("{")) {
                SendNotificationReq req = SendNotificationReq.builder()
                        .eventId("ORDER_CREATED_" + UUID.randomUUID())
                        .channel("EMAIL")
                        .recipient(payload)
                        .subject("Xác nhận đơn hàng")
                        .content("Đơn hàng của bạn đã được tạo thành công.")
                        .build();
                notificationService.sendNotification(req);
                return;
            }
            OrderCreated event = objectMapper.readValue(payload, OrderCreated.class);
            notificationService.sendNotification(notificationMapper.toReq(event));
        } catch (Exception e) {
            log.error("Lỗi khi xử lý sự kiện [order-created]: ", e);
        }
    }

    @KafkaListener(topics = "order-confirmed", groupId = "notify-group")
    public void listenerOrderConfirmed(String payload) {
        log.info("Nhận sự kiện [order-confirmed]: {}", payload);
        try {
            OrderConfirmed event = objectMapper.readValue(payload, OrderConfirmed.class);
            notificationService.sendNotification(notificationMapper.toReq(event));
        } catch (Exception e) {
            log.error("Lỗi khi xử lý sự kiện [order-confirmed]: ", e);
        }
    }

    @KafkaListener(topics = "order-status-changed", groupId = "notify-group")
    public void listenerOrderStatusChanged(String payload) {
        log.info("Nhận sự kiện [order-status-changed]: {}", payload);
        try {
            OrderStatusChanged event = objectMapper.readValue(payload, OrderStatusChanged.class);
            notificationService.sendNotification(notificationMapper.toReq(event));
        } catch (Exception e) {
            log.error("Lỗi khi xử lý sự kiện [order-status-changed]: ", e);
        }
    }

    @KafkaListener(topics = "order-cancelled", groupId = "notify-group")
    public void listenerOrderCancelled(String payload) {
        log.info("Nhận sự kiện [order-cancelled]: {}", payload);
        try {
            OrderCancelled event = objectMapper.readValue(payload, OrderCancelled.class);
            notificationService.sendNotification(notificationMapper.toReq(event));
        } catch (Exception e) {
            log.error("Lỗi khi xử lý sự kiện [order-cancelled]: ", e);
        }
    }

    @KafkaListener(topics = "payment-succeeded", groupId = "notify-group")
    public void listenerPaymentSucceeded(String payload) {
        log.info("Nhận sự kiện [payment-succeeded]: {}", payload);
        try {
            PaymentSucceeded event = objectMapper.readValue(payload, PaymentSucceeded.class);
            notificationService.sendNotification(notificationMapper.toReq(event));
        } catch (Exception e) {
            log.error("Lỗi khi xử lý sự kiện [payment-succeeded]: ", e);
        }
    }

    @KafkaListener(topics = "payment-failed", groupId = "notify-group")
    public void listenerPaymentFailed(String payload) {
        log.info("Nhận sự kiện [payment-failed]: {}", payload);
        try {
            PaymentFailed event = objectMapper.readValue(payload, PaymentFailed.class);
            notificationService.sendNotification(notificationMapper.toReq(event));
        } catch (Exception e) {
            log.error("Lỗi khi xử lý sự kiện [payment-failed]: ", e);
        }
    }

    @KafkaListener(topics = "refund-completed", groupId = "notify-group")
    public void listenerRefundCompleted(String payload) {
        log.info("Nhận sự kiện [refund-completed]: {}", payload);
        try {
            RefundCompleted event = objectMapper.readValue(payload, RefundCompleted.class);
            notificationService.sendNotification(notificationMapper.toReq(event));
        } catch (Exception e) {
            log.error("Lỗi khi xử lý sự kiện [refund-completed]: ", e);
        }
    }

    @KafkaListener(topics = {"user-registered", "user-registered-topic"}, groupId = "${spring.kafka.consumer.group-id:ecommerce-notification-group}")
    public void listenerUserRegistered(String payload) {
        log.info("Nhận sự kiện [user-registered]: {}", payload);
        try {
            UserRegistered event = objectMapper.readValue(payload, UserRegistered.class);
            notificationService.sendNotification(notificationMapper.toReq(event));
        } catch (Exception e) {
            log.error("Lỗi khi xử lý sự kiện [user-registered]: ", e);
        }
    }
}
