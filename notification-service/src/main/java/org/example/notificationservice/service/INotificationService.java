package org.example.notificationservice.service;

import org.example.notificationservice.dto.req.SendNotificationReq;
import org.example.notificationservice.dto.res.NotificationRes;
import org.example.notificationservice.dto.res.PageResponse;
import org.springframework.data.domain.Pageable;

public interface INotificationService {

    NotificationRes sendNotification(SendNotificationReq req);

    NotificationRes getNotificationById(String id);

    java.util.List<NotificationRes> getNotificationsByRecipient(String recipient);

    PageResponse<NotificationRes> getAllNotifications(String recipient, String status, Pageable pageable);

    void listenerAdapter(String message);
}
