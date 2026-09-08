package com.myga.learning.backend.backend.dto;

import com.myga.learning.backend.backend.models.PerformanceTrend;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * A student's academic performance, computed entirely from stored grades.
 * Averages are percentages, so grades on different scales stay comparable.
 * Fields are null/empty when there is not enough data — nothing is invented.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentPerformanceResponse {
    private StudentSummaryResponse student;
    private int totalGrades;
    /** Null when the student has no grades yet. */
    private Double overallAverage;
    private List<SubjectAverageResponse> subjectAverages;
    private List<SemesterAverageResponse> semesterAverages;
    private PerformanceTrend trend;
    /** Difference (percentage points) between the later and earlier halves. */
    private Double trendDelta;
}
