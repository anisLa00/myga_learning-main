package com.myga.learning.backend.backend.dto;

import com.myga.learning.backend.backend.models.AttendanceStatus;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/** Records a single attendance entry. */
@Data
public class AttendanceRequest {

    @NotNull(message = "studentId is required")
    private Long studentId;

    @NotNull(message = "classeId is required")
    private Long classeId;

    /** Optional subject/session context. */
    private Long subjectId;

    /** Only honoured for ADMIN callers; ignored for teachers. */
    private Long teacherId;

    /** Defaults to today when omitted. */
    private LocalDate date;

    @NotNull(message = "status is required")
    private AttendanceStatus status;

    private String note;
}
