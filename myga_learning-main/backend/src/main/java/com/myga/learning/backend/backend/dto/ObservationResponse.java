package com.myga.learning.backend.backend.dto;

import com.myga.learning.backend.backend.models.ObservationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObservationResponse {
    private Long id;
    private StudentSummaryResponse student;
    private Long teacherId;
    private String teacherName;
    private SubjectResponse subject;
    private ObservationType type;
    private String message;
    private LocalDate date;
    private boolean visibleToParents;
}
