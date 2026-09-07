package com.myga.learning.backend.backend.repositories;

import com.myga.learning.backend.backend.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipient_IdOrderByCreatedAtDesc(Long recipientId);

    long countByRecipient_IdAndReadAtIsNull(Long recipientId);
}
