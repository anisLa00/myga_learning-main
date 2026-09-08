package com.myga.learning.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Aggregate view for the administrator dashboard. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {
    private long totalStudents;
    private long totalTeachers;
    private long totalParents;
    private long totalClasses;
    private long totalSubjects;

    private List<GradeResponse> recentGrades;
    private List<AttendanceResponse> recentAttendance;
    /** Recent absences, surfaced as alerts. */
    private List<AttendanceResponse> recentAbsences;
    private List<AnnouncementResponse> recentAnnouncements;
}
