package com.myga.learning.backend.backend.controller;

import com.myga.learning.backend.backend.dto.AttendanceBulkRequest;
import com.myga.learning.backend.backend.dto.AttendanceRequest;
import com.myga.learning.backend.backend.dto.AttendanceResponse;
import com.myga.learning.backend.backend.dto.AttendanceSummaryResponse;
import com.myga.learning.backend.backend.service.AttendanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /** Record a single attendance entry (ADMIN or the assigned TEACHER). */
    @PostMapping("/attendance")
    public ResponseEntity<AttendanceResponse> record(@Valid @RequestBody AttendanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.record(request));
    }

    /** Mark attendance for a whole class in one request. */
    @PostMapping("/attendance/bulk")
    public ResponseEntity<List<AttendanceResponse>> markClass(@Valid @RequestBody AttendanceBulkRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.markClass(request));
    }

    /** A student's attendance records and summary. Ownership is enforced server-side. */
    @GetMapping("/students/{studentId}/attendance")
    public AttendanceSummaryResponse getStudentAttendance(@PathVariable Long studentId) {
        return attendanceService.getStudentAttendance(studentId);
    }

    /** The register for a class on a date (ADMIN or a teacher of that class). */
    @GetMapping("/classes/{classeId}/attendance")
    public List<AttendanceResponse> getClassAttendance(
            @PathVariable Long classeId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return attendanceService.getClassAttendance(classeId, date != null ? date : LocalDate.now());
    }
}
