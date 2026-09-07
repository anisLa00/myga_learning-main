package com.myga.learning.backend.backend.controller;

import com.myga.learning.backend.backend.dto.NotificationResponse;
import com.myga.learning.backend.backend.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** Each user manages only their own notifications. */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/me")
    public List<NotificationResponse> myNotifications() {
        return notificationService.listMine();
    }

    @GetMapping("/me/unread-count")
    public Map<String, Long> unreadCount() {
        return Map.of("unread", notificationService.unreadCountMine());
    }

    @PutMapping("/{id}/read")
    public NotificationResponse markRead(@PathVariable Long id) {
        return notificationService.markRead(id);
    }

    @PutMapping("/me/read-all")
    public ResponseEntity<Void> markAllRead() {
        notificationService.markAllReadMine();
        return ResponseEntity.noContent().build();
    }
}
