package com.myga.learning.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Compact per-child attendance counters for the parent dashboard (without the
 * full record list, which is available from the attendance endpoint).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChildAttendanceStatsResponse {
    private StudentSummaryResponse student;
    private int total;
    private int totalPresent;
    private int totalAbsent;
    private int totalLate;
    private int totalExcused;
    private double attendancePercentage;
}
