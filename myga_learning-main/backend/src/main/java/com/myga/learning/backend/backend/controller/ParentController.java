package com.myga.learning.backend.backend.controller;

import com.myga.learning.backend.backend.dto.ParentRequest;
import com.myga.learning.backend.backend.dto.ParentResponse;
import com.myga.learning.backend.backend.service.ParentService;
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
@RequestMapping("/api/parents")
public class ParentController {

    private final ParentService parentService;

    public ParentController(ParentService parentService) {
        this.parentService = parentService;
    }

    @GetMapping
    public List<ParentResponse> findAll() {
        return parentService.findAll();
    }

    @GetMapping("/{phone}")
    public ParentResponse findByPhone(@PathVariable Long phone) {
        return parentService.findByPhone(phone);
    }

    @PostMapping
    public ResponseEntity<ParentResponse> create(@Valid @RequestBody ParentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(parentService.create(request));
    }

    @PutMapping("/{phone}")
    public ParentResponse update(@PathVariable Long phone, @Valid @RequestBody ParentRequest request) {
        return parentService.update(phone, request);
    }

    @DeleteMapping("/{phone}")
    public ResponseEntity<Void> delete(@PathVariable Long phone) {
        parentService.delete(phone);
        return ResponseEntity.noContent().build();
    }

    /** Link an existing student to this parent as one of their children (admin). */
    @PostMapping("/{phone}/students/{studentId}")
    public ParentResponse linkStudent(@PathVariable Long phone, @PathVariable Long studentId) {
        return parentService.linkStudent(phone, studentId);
    }

    /** Remove the link between this parent and one of their children (admin). */
    @DeleteMapping("/{phone}/students/{studentId}")
    public ParentResponse unlinkStudent(@PathVariable Long phone, @PathVariable Long studentId) {
        return parentService.unlinkStudent(phone, studentId);
    }
}
