package com.myga.learning.backend.backend.mapper;

import com.myga.learning.backend.backend.dto.AnnouncementResponse;
import com.myga.learning.backend.backend.models.Announcement;

/** Maps {@link Announcement} entities to their API response DTOs. */
public final class AnnouncementMapper {

    private AnnouncementMapper() {
    }

    public static AnnouncementResponse toResponse(Announcement a) {
        if (a == null) {
            return null;
        }
        return AnnouncementResponse.builder()
                .id(a.getId())
                .title(a.getTitle())
                .message(a.getMessage())
                .target(a.getTarget())
                .classeId(a.getTargetClasse() == null ? null : a.getTargetClasse().getId())
                .targetUserEmail(a.getTargetUser() == null ? null : a.getTargetUser().getEmail())
                .createdByEmail(a.getCreatedBy() == null ? null : a.getCreatedBy().getEmail())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
