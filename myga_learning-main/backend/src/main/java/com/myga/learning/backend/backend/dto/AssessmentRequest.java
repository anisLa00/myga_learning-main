package com.myga.learning.backend.backend.dto;

import com.myga.learning.backend.backend.models.AssessmentType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDate;

/** Payload to schedule/record an assessment for a class in a subject. */
@Data
public class AssessmentRequest {

    @NotBlank(message = "title is required")
    private String title;

    private String description;

    @NotNull(message = "subjectId is required")
    private Long subjectId;

    @NotNull(message = "classeId is required")
    private Long classeId;

    /** Only honoured for ADMIN callers; ignored for teachers. */
    private Long teacherId;

    @NotNull(message = "type is required")
    private AssessmentType type;

    /** Defaults to today when omitted. */
    private LocalDate date;

    @NotNull(message = "maxGrade is required")
    @Positive(message = "maxGrade must be positive")
    private Double maxGrade;

    /** Optional; the academic year follows from the semester. */
    private Long semesterId;
}
