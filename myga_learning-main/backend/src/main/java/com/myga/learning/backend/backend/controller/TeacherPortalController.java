package com.myga.learning.backend.backend.controller;

import com.myga.learning.backend.backend.dto.ClasseSummaryResponse;
import com.myga.learning.backend.backend.dto.SubjectResponse;
import com.myga.learning.backend.backend.service.TeacherService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** The authenticated teacher's own workspace. */
@RestController
@RequestMapping("/api/teacher")
public class TeacherPortalController {

    private final TeacherService teacherService;

    public TeacherPortalController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping("/me/subjects")
    public List<SubjectResponse> mySubjects() {
        return teacherService.getMySubjects();
    }

    @GetMapping("/me/classes")
    public List<ClasseSummaryResponse> myClasses() {
        return teacherService.getMyClasses();
    }
}
