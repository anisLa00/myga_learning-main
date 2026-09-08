package com.myga.learning.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Aggregate view for the teacher's own workspace. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherDashboardResponse {
    private List<ClasseSummaryResponse> classes;
    private List<SubjectResponse> subjects;
    /** Students across the teacher's assigned classes. */
    private long totalStudents;

    private List<GradeResponse> recentGrades;
    private List<ObservationResponse> recentObservations;
    private List<AnnouncementResponse> announcements;
    private long unreadNotifications;
}
