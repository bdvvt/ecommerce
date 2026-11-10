package org.example.notificationservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notificationservice.dto.mapper.NotificationMapper;
import org.example.notificationservice.dto.req.SendNotificationReq;
import org.example.notificationservice.dto.res.NotificationRes;
import org.example.notificationservice.dto.res.PageResponse;
import org.example.notificationservice.entity.NotificationLog;
import org.example.notificationservice.exceptions.NotFoundException;
import org.example.notificationservice.repository.NotificationLogRepository;
import org.example.notificationservice.service.IMailService;
import org.example.notificationservice.service.INotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements INotificationService {

    private final IMailService mailService;
    private final NotificationLogRepository notificationLogRepository;
    @Autowired(required = false)
    private final StringRedisTemplate redisTemplate;
    private final NotificationMapper notificationMapper;

    @Override
    public NotificationRes sendNotification(SendNotificationReq req) {
        String eventId = req.getEventId();

        // FR-NOT-007: Xử lý event trùng không gửi lặp (Idempotency qua Redis nếu khả dụng)
        if (StringUtils.hasText(eventId) && redisTemplate != null) {
            try {
                String redisKey = "notif:idemp:" + eventId;
                Boolean isNew = redisTemplate.opsForValue().setIfAbsent(redisKey, "1", Duration.ofDays(7));
                if (Boolean.FALSE.equals(isNew)) {
                    return notificationLogRepository.findByEventId(eventId)
                            .map(notificationMapper::toResponse)
                            .orElse(null);
                }
            } catch (Exception e) {
                log.warn("⚠️ Redis không sẵn sàng, bỏ qua kiểm tra trùng event: {}", e.getMessage());
            }
        }

        // Chuyển đổi từ DTO Request sang Entity bằng MapStruct
        NotificationLog notificationLog = notificationMapper.toEntity(req);

        // FR-NOT-008: Lỗi gửi mail không được làm gián đoạn transaction chính
        try {
            mailService.sendEmail(req.getRecipient(), req.getSubject(), req.getContent());
            notificationLog.setStatus("SENT");
            log.info("✅ Đã gửi email thông báo thành công tới: {}", req.getRecipient());
        } catch (Exception e) {
            notificationLog.setStatus("FAILED");
            log.error("❌ Lỗi khi gửi email thông báo tới {}: {}", req.getRecipient(), e.getMessage());
        }

        // FR-NOT-005: Lưu lại nhật ký thông báo vào MongoDB (nếu khả dụng)
        try {
            NotificationLog saved = notificationLogRepository.save(notificationLog);
            return notificationMapper.toResponse(saved);
        } catch (Exception e) {
            log.error("❌ Không thể lưu nhật ký thông báo vào Database: {}", e.getMessage());
            return notificationMapper.toResponse(notificationLog);
        }
    }

    @Override
    public NotificationRes getNotificationById(String id) {
        NotificationLog notificationLog = notificationLogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy nhật ký thông báo với ID: " + id));
        return notificationMapper.toResponse(notificationLog);
    }

    @Override
    public List<NotificationRes> getNotificationsByRecipient(String recipient) {
        if (!StringUtils.hasText(recipient)) {
            return Collections.emptyList();
        }
        try {
            return notificationLogRepository.findByRecipient(recipient).stream()
                    .map(notificationMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm thông báo theo người nhận {}: {}", recipient, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public PageResponse<NotificationRes> getAllNotifications(String recipient, String status, Pageable pageable) {
        Page<NotificationLog> page;
        if (StringUtils.hasText(recipient)) {
            page = notificationLogRepository.findByRecipient(recipient, pageable);
        } else if (StringUtils.hasText(status)) {
            page = notificationLogRepository.findByStatus(status, pageable);
        } else {
            page = notificationLogRepository.findAll(pageable);
        }
        List<NotificationRes> items = notificationMapper.toResponseList(page.getContent());
        return PageResponse.of(page, items);
    }

    @Override
    public void listenerAdapter(String message) {
        SendNotificationReq req = SendNotificationReq.builder()
                .eventId("REDIS_" + UUID.randomUUID())
                .channel("EMAIL")
                .recipient(message)
                .subject("Xác nhận đơn hàng")
                .content("Vui lòng bạn xác nhận đơn hàng này thông qua luồng mail này")
                .build();
        sendNotification(req);
    }
}
