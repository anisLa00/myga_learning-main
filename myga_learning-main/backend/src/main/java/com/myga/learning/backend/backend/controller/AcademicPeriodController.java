package com.myga.learning.backend.backend.controller;

import com.myga.learning.backend.backend.dto.AcademicYearRequest;
import com.myga.learning.backend.backend.dto.AcademicYearResponse;
import com.myga.learning.backend.backend.dto.SemesterRequest;
import com.myga.learning.backend.backend.dto.SemesterResponse;
import com.myga.learning.backend.backend.service.AcademicPeriodService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/** Academic years and semesters. Writes are administrator-only. */
@RestController
@RequestMapping("/api")
public class AcademicPeriodController {

    private final AcademicPeriodService academicPeriodService;

    public AcademicPeriodController(AcademicPeriodService academicPeriodService) {
        this.academicPeriodService = academicPeriodService;
    }

    @GetMapping("/academic-years")
    public List<AcademicYearResponse> years() {
        return academicPeriodService.findYears();
    }

    @GetMapping("/academic-years/{id}")
    public AcademicYearResponse year(@PathVariable Long id) {
        return academicPeriodService.findYear(id);
    }

    @PostMapping("/academic-years")
    public ResponseEntity<AcademicYearResponse> createYear(@Valid @RequestBody AcademicYearRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(academicPeriodService.createYear(request));
    }

    @PutMapping("/academic-years/{id}/current")
    public AcademicYearResponse setCurrent(@PathVariable Long id) {
        return academicPeriodService.setCurrentYear(id);
    }

    @GetMapping("/semesters")
    public List<SemesterResponse> semesters(@RequestParam(required = false) Long academicYearId) {
        return academicPeriodService.findSemesters(academicYearId);
    }

    @GetMapping("/semesters/{id}")
    public SemesterResponse semester(@PathVariable Long id) {
        return academicPeriodService.findSemester(id);
    }

    @PostMapping("/semesters")
    public ResponseEntity<SemesterResponse> createSemester(@Valid @RequestBody SemesterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(academicPeriodService.createSemester(request));
    }
}
