package com.myga.learning.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Aggregate view for a parent, covering only their own children. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentDashboardResponse {
    private List<StudentSummaryResponse> children;
    private List<ChildAttendanceStatsResponse> attendance;

    private List<GradeResponse> latestGrades;
    private List<AttendanceResponse> recentAbsences;
    /** Parent-visible teacher feedback only. */
    private List<ObservationResponse> recentFeedback;
    private List<AnnouncementResponse> announcements;
    private long unreadNotifications;
}
