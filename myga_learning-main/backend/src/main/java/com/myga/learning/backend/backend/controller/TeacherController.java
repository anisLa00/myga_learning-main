package com.myga.learning.backend.backend.controller;

import com.myga.learning.backend.backend.dto.TeacherRequest;
import com.myga.learning.backend.backend.dto.TeacherResponse;
import com.myga.learning.backend.backend.service.TeacherService;
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

/** Administrator endpoints for managing teachers and their assignments. */
@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping
    public List<TeacherResponse> findAll() {
        return teacherService.findAll();
    }

    @GetMapping("/{id}")
    public TeacherResponse findById(@PathVariable Long id) {
        return teacherService.findById(id);
    }

    @PostMapping
    public ResponseEntity<TeacherResponse> create(@Valid @RequestBody TeacherRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teacherService.create(request));
    }

    @PostMapping("/{id}/subjects/{subjectId}")
    public TeacherResponse assignSubject(@PathVariable Long id, @PathVariable Long subjectId) {
        return teacherService.assignSubject(id, subjectId);
    }

    @PostMapping("/{id}/classes/{classeId}")
    public TeacherResponse assignClasse(@PathVariable Long id, @PathVariable Long classeId) {
        return teacherService.assignClasse(id, classeId);
    }

    @PutMapping("/{id}")
    public TeacherResponse update(@PathVariable Long id, @Valid @RequestBody TeacherRequest request) {
        return teacherService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        teacherService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/subjects/{subjectId}")
    public TeacherResponse unassignSubject(@PathVariable Long id, @PathVariable Long subjectId) {
        return teacherService.unassignSubject(id, subjectId);
    }

    @DeleteMapping("/{id}/classes/{classeId}")
    public TeacherResponse unassignClasse(@PathVariable Long id, @PathVariable Long classeId) {
        return teacherService.unassignClasse(id, classeId);
    }
}
