package com.myga.learning.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * A student's attendance records plus aggregate counts. The percentage counts
 * PRESENT and LATE as attended, out of all recorded sessions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSummaryResponse {
    private List<AttendanceResponse> records;
    private int total;
    private int totalPresent;
    private int totalAbsent;
    private int totalLate;
    private int totalExcused;
    private double attendancePercentage;
}
