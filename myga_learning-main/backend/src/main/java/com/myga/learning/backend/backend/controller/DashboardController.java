package com.myga.learning.backend.backend.controller;

import com.myga.learning.backend.backend.dto.AdminDashboardResponse;
import com.myga.learning.backend.backend.dto.ParentDashboardResponse;
import com.myga.learning.backend.backend.dto.TeacherDashboardResponse;
import com.myga.learning.backend.backend.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Role dashboards. Each endpoint is restricted to its role in the security
 * config, and the teacher/parent views are additionally scoped to the caller's
 * own assignments or children.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/admin")
    public AdminDashboardResponse admin() {
        return dashboardService.adminDashboard();
    }

    @GetMapping("/teacher")
    public TeacherDashboardResponse teacher() {
        return dashboardService.teacherDashboard();
    }

    @GetMapping("/parent")
    public ParentDashboardResponse parent() {
        return dashboardService.parentDashboard();
    }
}
