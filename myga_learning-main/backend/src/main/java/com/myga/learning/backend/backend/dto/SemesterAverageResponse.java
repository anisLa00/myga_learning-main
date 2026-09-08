package com.myga.learning.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Average for one semester, expressed as a percentage of the maximum. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemesterAverageResponse {
    private Long semesterId;
    private String semester;
    private String academicYear;
    private int gradeCount;
    /** 0-100, the mean of each grade's value/maxValue. */
    private double average;
}
