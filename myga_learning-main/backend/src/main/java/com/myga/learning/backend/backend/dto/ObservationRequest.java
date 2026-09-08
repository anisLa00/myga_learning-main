package com.myga.learning.backend.backend.dto;

import com.myga.learning.backend.backend.models.ObservationType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/** Payload for a teacher (or admin) to record an observation about a student. */
@Data
public class ObservationRequest {

    @NotNull(message = "studentId is required")
    private Long studentId;

    /** Optional subject context. */
    private Long subjectId;

    /** Only honoured for ADMIN callers; ignored for teachers. */
    private Long teacherId;

    @NotNull(message = "type is required")
    private ObservationType type;

    @NotBlank(message = "message is required")
    private String message;

    /** Defaults to today when omitted. */
    private LocalDate date;

    /** Defaults to true (visible to parents) when omitted. */
    private Boolean visibleToParents;
}
