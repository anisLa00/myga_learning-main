package com.myga.learning.backend.backend.dto;

import com.myga.learning.backend.backend.models.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {
    private Long id;
    private StudentSummaryResponse student;
    private ClasseSummaryResponse classe;
    private SubjectResponse subject;
    private Long teacherId;
    private String teacherName;
    private LocalDate date;
    private AttendanceStatus status;
    private String note;
}
