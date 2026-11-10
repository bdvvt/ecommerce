package com.example.identityservice.client;

import com.example.identityservice.client.dto.NotificationHistoryDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

/**
 * Client dùng để LẤY DỮ LIỆU từ notification-service (Đồng bộ - Synchronous) qua Spring RestClient.
 * 
 * QUY TẮC KIẾN TRÚC:
 * - CLIENT: Chỉ dùng để lấy dữ liệu (query / fetch data) từ service khác khi cần ngay kết quả.
 * - EVENT (Kafka): Dùng khi phát sinh sự kiện cần xử lý bất đồng bộ (như bắn event để gửi thông báo/email).
 */
@Component
public class NotificationClient {

    private final RestClient restClient;

    public NotificationClient(@Value("${notification.service.url:http://localhost:8087}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    /**
     * Lấy lịch sử thông báo của một người dùng từ notification-service
     */
    public List<NotificationHistoryDto> getNotificationHistoryByUserId(Long userId) {
        try {
            return restClient.get()
                    .uri("/api/v1/notifications/user/{userId}", userId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<NotificationHistoryDto>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
