package com.myga.learning.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Average for one subject, expressed as a percentage of the maximum. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectAverageResponse {
    private SubjectResponse subject;
    private int gradeCount;
    /** 0-100, the mean of each grade's value/maxValue. */
    private double average;
}
