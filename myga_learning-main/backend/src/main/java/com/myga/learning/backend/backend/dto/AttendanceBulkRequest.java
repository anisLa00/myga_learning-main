package com.myga.learning.backend.backend.dto;

import com.myga.learning.backend.backend.models.AttendanceStatus;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/** Marks attendance for a whole class in one request (the teacher's screen). */
@Data
public class AttendanceBulkRequest {

    @NotNull(message = "classeId is required")
    private Long classeId;

    private Long subjectId;

    /** Only honoured for ADMIN callers; ignored for teachers. */
    private Long teacherId;

    /** Defaults to today when omitted. */
    private LocalDate date;

    @NotEmpty(message = "at least one entry is required")
    @Valid
    private List<Entry> entries;

    @Data
    public static class Entry {
        @NotNull(message = "studentId is required")
        private Long studentId;

        @NotNull(message = "status is required")
        private AttendanceStatus status;

        private String note;
    }
}
