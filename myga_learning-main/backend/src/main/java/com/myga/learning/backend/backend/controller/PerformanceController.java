package com.myga.learning.backend.backend.controller;

import com.myga.learning.backend.backend.dto.StudentPerformanceResponse;
import com.myga.learning.backend.backend.service.PerformanceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PerformanceController {

    private final PerformanceService performanceService;

    public PerformanceController(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    /** Subject/semester averages and trend for a student. Ownership-checked. */
    @GetMapping("/students/{studentId}/performance")
    public StudentPerformanceResponse studentPerformance(@PathVariable Long studentId) {
        return performanceService.getStudentPerformance(studentId);
    }
}
