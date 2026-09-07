package com.myga.learning.backend.backend.mapper;

import com.myga.learning.backend.backend.dto.NotificationResponse;
import com.myga.learning.backend.backend.models.Notification;

/** Maps {@link Notification} entities to their API response DTOs. */
public final class NotificationMapper {

    private NotificationMapper() {
    }

    public static NotificationResponse toResponse(Notification n) {
        if (n == null) {
            return null;
        }
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .createdAt(n.getCreatedAt())
                .readAt(n.getReadAt())
                .read(n.getReadAt() != null)
                .build();
    }
}
