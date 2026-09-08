package com.myga.learning.backend.backend.controller;

import com.myga.learning.backend.backend.dto.AssessmentRequest;
import com.myga.learning.backend.backend.dto.AssessmentResponse;
import com.myga.learning.backend.backend.service.AssessmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    /** Schedule an assessment (ADMIN, or a teacher of that class and subject). */
    @PostMapping("/assessments")
    public ResponseEntity<AssessmentResponse> create(@Valid @RequestBody AssessmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assessmentService.create(request));
    }

    @GetMapping("/assessments/{id}")
    public AssessmentResponse findById(@PathVariable Long id) {
        return assessmentService.findById(id);
    }

    /** Assessments for a student's class; pass ?upcoming=true for future ones only. */
    @GetMapping("/students/{studentId}/assessments")
    public List<AssessmentResponse> forStudent(@PathVariable Long studentId,
                                               @RequestParam(defaultValue = "false") boolean upcoming) {
        return assessmentService.findForStudent(studentId, upcoming);
    }

    /** The authenticated teacher's own assessments. */
    @GetMapping("/teacher/me/assessments")
    public List<AssessmentResponse> mine() {
        return assessmentService.findMine();
    }
}
