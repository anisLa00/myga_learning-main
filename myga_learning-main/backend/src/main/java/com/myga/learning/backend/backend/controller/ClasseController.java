package com.myga.learning.backend.backend.controller;

import com.myga.learning.backend.backend.dto.ClasseRequest;
import com.myga.learning.backend.backend.dto.ClasseResponse;
import com.myga.learning.backend.backend.service.ClasseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
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

    @PostMapping
    public ResponseEntity<ClasseResponse> create(@Valid @RequestBody ClasseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(classeService.create(request));
    }

    @GetMapping("/{id}")
    public ClasseResponse findById(@PathVariable Long id) {
        return classeService.findById(id);
    }
}
