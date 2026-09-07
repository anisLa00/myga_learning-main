package com.myga.learning.backend.backend.controller;

import com.myga.learning.backend.backend.dto.ClasseResponse;
import com.myga.learning.backend.backend.service.ClasseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
public class ClasseController {

    private final ClasseService classeService;

    public ClasseController(ClasseService classeService) {
        this.classeService = classeService;
    }

    @GetMapping
    public List<ClasseResponse> findAll() {
        return classeService.findAll();
    }

    @GetMapping("/{id}")
    public ClasseResponse findById(@PathVariable Long id) {
        return classeService.findById(id);
    }
}
