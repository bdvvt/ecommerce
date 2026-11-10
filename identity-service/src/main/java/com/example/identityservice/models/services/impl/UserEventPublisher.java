package com.example.identityservice.models.services.impl;

import com.example.identityservice.models.entities.User;
import com.example.identityservice.models.events.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Event Publisher chuyên phát sự kiện bất đồng bộ qua Kafka.
 * Tuân thủ quy tắc kiến trúc:
 * - EVENT (Kafka): Xử lý bất đồng bộ các sự kiện thay đổi dữ liệu, thông báo (Notification/Email/Audit).
 * - CLIENT: Dùng khi cần gọi đồng bộ để LẤY DỮ LIỆU từ service khác.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    @Value("${spring.kafka.topics.user-registered:user-registered-topic}")
    private String userRegisteredTopic;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishUserRegistered(User user, String otp) {
        if (user == null) {
            log.warn("⚠️ Không thể publish sự kiện USER_REGISTERED do user là null");
            return;
        }

        String recipientEmail = user.getEmail();
        if (recipientEmail == null || recipientEmail.isBlank()) {
            if (user.getLoginIdentifier() != null && user.getLoginIdentifier().contains("@")) {
                recipientEmail = user.getLoginIdentifier();
            }
        }

        if (recipientEmail == null || recipientEmail.isBlank()) {
            log.warn("⚠️ Không tìm thấy email của user {}, bỏ qua gửi thông báo xác thực", user.getLoginIdentifier());
            return;
        }

        final String finalEmail = recipientEmail;
        CompletableFuture.runAsync(() -> {
            try {
                UserRegisteredEvent event = UserRegisteredEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .eventType("USER_REGISTERED")
                        .userId(user.getUserId())
                        .email(finalEmail)
                        .fullName(user.getFullName() != null ? user.getFullName() : user.getLoginIdentifier())
                        .loginIdentifier(user.getLoginIdentifier())
                        .otp(otp)
                        .timestamp(Instant.now().toString())
                        .build();

                sendEvent(event, String.valueOf(user.getUserId()));
            } catch (Exception e) {
                log.error("❌ Ngoại lệ khi khởi tạo sự kiện USER_REGISTERED: {}", e.getMessage(), e);
            }
        });
    }

    public void publishOtpEmail(String email, String fullName, String otp, String type) {
        if (email == null || email.isBlank()) {
            log.warn("⚠️ Email nhận rỗng, bỏ qua gửi OTP {}", type);
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                UserRegisteredEvent event = UserRegisteredEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .eventType(type)
                        .email(email)
                        .fullName(fullName != null ? fullName : email)
                        .loginIdentifier(email)
                        .otp(otp)
                        .timestamp(Instant.now().toString())
                        .build();

                sendEvent(event, email);
            } catch (Exception e) {
                log.error("❌ Ngoại lệ khi gửi sự kiện OTP qua Kafka: {}", e.getMessage(), e);
            }
        });
    }

    private void sendEvent(UserRegisteredEvent event, String key) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(event);
            log.info("🚀 Đang publish sự kiện {} vào Kafka topic '{}' cho email: {}",
                    event.getEventType(), userRegisteredTopic, event.getEmail());

            kafkaTemplate.send(userRegisteredTopic, key, jsonPayload)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("✅ Đã publish sự kiện {} thành công vào Kafka topic '{}' cho email: {}",
                                    event.getEventType(), userRegisteredTopic, event.getEmail());
                        } else {
                            log.error("❌ Lỗi khi gửi sự kiện {} tới Kafka: {}", event.getEventType(), ex.getMessage(), ex);
                        }
                    });
        } catch (Exception e) {
            log.error("❌ Lỗi khi serialize sự kiện {} sang JSON: {}", event.getEventType(), e.getMessage(), e);
        }
    }
}
