package com.myga.learning.backend.backend.dto;

import com.myga.learning.backend.backend.models.AssessmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentResponse {
    private Long id;
    private String title;
    private String description;
    private SubjectResponse subject;
    private ClasseSummaryResponse classe;
    private Long teacherId;
    private String teacherName;
    private AssessmentType type;
    private LocalDate date;
    private double maxGrade;
    private Long semesterId;
    private String semester;
    /** Derived from the semester. */
    private String academicYear;
}
