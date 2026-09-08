package com.myga.learning.backend.backend.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

/**
 * Payload to record a grade. The teacher is derived from the authenticated
 * user for TEACHER callers and must never be trusted from the client; an ADMIN
 * caller may set {@code teacherId} explicitly.
 */
@Data
public class GradeRequest {

    @NotNull(message = "studentId is required")
    private Long studentId;

    /**
     * Optional. When set, the subject, maximum grade and semester are taken
     * from the assessment so they can never disagree with it.
     */
    private Long assessmentId;

    /** Required unless {@code assessmentId} is provided. */
    private Long subjectId;

    /** Optional; the academic year follows from the semester. */
    private Long semesterId;

    /** Only honoured for ADMIN callers; ignored for teachers. */
    private Long teacherId;

    @NotNull(message = "value is required")
    @PositiveOrZero(message = "value must be zero or positive")
    private Double value;

    /** Required unless {@code assessmentId} is provided. */
    @Positive(message = "maxValue must be positive")
    private Double maxValue;

    private String comment;

    /** Defaults to today when omitted. */
    private LocalDate date;
}
