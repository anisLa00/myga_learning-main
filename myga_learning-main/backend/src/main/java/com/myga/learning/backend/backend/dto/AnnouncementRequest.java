package com.myga.learning.backend.backend.dto;

import com.myga.learning.backend.backend.models.AnnouncementTarget;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/** Admin payload to publish an announcement. */
@Data
public class AnnouncementRequest {

    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "message is required")
    private String message;

    @NotNull(message = "target is required")
    private AnnouncementTarget target;

    /** Required when target == CLASS. */
    private Long classeId;

    /** Required when target == USER. */
    private Long userId;
}
