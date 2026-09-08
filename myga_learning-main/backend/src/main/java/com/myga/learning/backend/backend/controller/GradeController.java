package com.myga.learning.backend.backend.controller;

import com.myga.learning.backend.backend.dto.GradeRequest;
import com.myga.learning.backend.backend.dto.GradeResponse;
import com.myga.learning.backend.backend.service.GradeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
public class GradeController {

    private final GradeService gradeService;

    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    /** Record a grade. TEACHER callers are restricted to their own assignments. */
    @PostMapping("/grades")
    public ResponseEntity<GradeResponse> create(@Valid @RequestBody GradeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gradeService.create(request));
    }

    /** Grades for one student. Ownership is enforced server-side. */
    @GetMapping("/students/{studentId}/grades")
    public List<GradeResponse> getStudentGrades(@PathVariable Long studentId) {
        return gradeService.getStudentGrades(studentId);
    }

    /** Update a grade. A teacher may only change grades they recorded. */
    @PutMapping("/grades/{id}")
    public GradeResponse update(@PathVariable Long id, @Valid @RequestBody GradeRequest request) {
        return gradeService.update(id, request);
    }

    /** Delete a grade, under the same "own grades only" rule. */
    @DeleteMapping("/grades/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        gradeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
