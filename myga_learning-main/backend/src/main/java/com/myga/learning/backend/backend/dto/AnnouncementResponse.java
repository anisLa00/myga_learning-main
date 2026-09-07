package com.myga.learning.backend.backend.dto;

import com.myga.learning.backend.backend.models.AnnouncementTarget;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementResponse {
    private Long id;
    private String title;
    private String message;
    private AnnouncementTarget target;
    private Long classeId;
    private String targetUserEmail;
    private String createdByEmail;
    private LocalDateTime createdAt;
}
