package com.myga.learning.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeResponse {
    private Long id;
    private StudentSummaryResponse student;
    private SubjectResponse subject;
    private Long teacherId;
    private String teacherName;
    private double value;
    private double maxValue;
    private String comment;
    private LocalDate date;
}
