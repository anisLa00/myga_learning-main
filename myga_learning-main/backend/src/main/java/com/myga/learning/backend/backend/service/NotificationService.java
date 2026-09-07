package com.myga.learning.backend.backend.service;

import com.myga.learning.backend.backend.dto.NotificationResponse;
import com.myga.learning.backend.backend.exception.ResourceNotFoundException;
import com.myga.learning.backend.backend.mapper.NotificationMapper;
import com.myga.learning.backend.backend.models.Notification;
import com.myga.learning.backend.backend.models.NotificationType;
import com.myga.learning.backend.backend.models.Parent;
import com.myga.learning.backend.backend.models.Student;
import com.myga.learning.backend.backend.models.User;
import com.myga.learning.backend.backend.repositories.NotificationRepository;
import com.myga.learning.backend.backend.repositories.ParentRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/** Creates and serves in-app notifications. */
@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ParentRepository parentRepository;
    private final CurrentUserService currentUserService;

    public NotificationService(NotificationRepository notificationRepository,
                               ParentRepository parentRepository,
                               CurrentUserService currentUserService) {
        this.notificationRepository = notificationRepository;
        this.parentRepository = parentRepository;
        this.currentUserService = currentUserService;
    }

    /** Persists a notification for a single recipient. */
    public void notifyUser(User recipient, NotificationType type, String title, String message) {
        if (recipient == null) {
            return;
        }
        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(n);
    }

    /** Notifies every parent account linked to the given student. */
    public void notifyStudentParents(Student student, NotificationType type, String title, String message) {
        List<Parent> parents = parentRepository.findDistinctByStudents_Id(student.getId());
        for (Parent parent : parents) {
            notifyUser(parent.getUser(), type, title, message);
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listMine() {
        Long userId = currentUserService.getCurrentUser().getId();
        return notificationRepository.findByRecipient_IdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long unreadCountMine() {
        return notificationRepository.countByRecipient_IdAndReadAtIsNull(currentUserService.getCurrentUser().getId());
    }

    public NotificationResponse markRead(Long id) {
        Long userId = currentUserService.getCurrentUser().getId();
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Notification", id));
        if (!n.getRecipient().getId().equals(userId)) {
            throw new AccessDeniedException("This notification does not belong to you");
        }
        if (n.getReadAt() == null) {
            n.setReadAt(LocalDateTime.now());
            notificationRepository.save(n);
        }
        return NotificationMapper.toResponse(n);
    }

    public void markAllReadMine() {
        Long userId = currentUserService.getCurrentUser().getId();
        LocalDateTime now = LocalDateTime.now();
        List<Notification> unread = notificationRepository.findByRecipient_IdOrderByCreatedAtDesc(userId).stream()
                .filter(n -> n.getReadAt() == null)
                .collect(Collectors.toList());
        unread.forEach(n -> n.setReadAt(now));
        notificationRepository.saveAll(unread);
    }
}
