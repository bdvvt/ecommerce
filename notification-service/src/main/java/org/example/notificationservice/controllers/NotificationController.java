package org.example.notificationservice.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.notificationservice.dto.req.SendNotificationReq;
import org.example.notificationservice.dto.res.NotificationRes;
import org.example.notificationservice.dto.res.PageResponse;
import org.example.notificationservice.dto.wrapper.ApiResponse;
import org.example.notificationservice.service.INotificationService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/notifications", "/api/notifications"})
@RequiredArgsConstructor
public class NotificationController {

    private final INotificationService notificationService;

    @PostMapping({"/send", ""})
    public ApiResponse<NotificationRes> sendNotification(@Valid @RequestBody SendNotificationReq req) {
        NotificationRes res = notificationService.sendNotification(req);
        return ApiResponse.success("Gửi thông báo thành công", res);
    }

    @GetMapping("/{id}")
    public ApiResponse<NotificationRes> getNotificationById(@PathVariable("id") String id) {
        return ApiResponse.success(notificationService.getNotificationById(id));
    }

    /**
     * Endpoint cung cấp cho OpenFeign Client của identity-service hoặc các service khác gọi đồng bộ.
     */
    @GetMapping("/user/{userId}")
    public List<NotificationRes> getNotificationHistoryByUserId(@PathVariable("userId") String userId) {
        return notificationService.getNotificationsByRecipient(userId);
    }

    @GetMapping
    public ApiResponse<PageResponse<NotificationRes>> getAllNotifications(
            @RequestParam(required = false) String recipient,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponse.success(notificationService.getAllNotifications(recipient, status, pageable));
    }
}
