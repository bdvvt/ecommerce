package org.example.notificationservice.repository;

import org.example.notificationservice.entity.NotificationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationLogRepository extends MongoRepository<NotificationLog, String> {

    Optional<NotificationLog> findByEventId(String eventId);

    List<NotificationLog> findByRecipient(String recipient);

    Page<NotificationLog> findByRecipient(String recipient, Pageable pageable);

    Page<NotificationLog> findByStatus(String status, Pageable pageable);
}
