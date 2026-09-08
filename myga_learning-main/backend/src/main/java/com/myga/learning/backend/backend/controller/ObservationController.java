package com.myga.learning.backend.backend.controller;

import com.myga.learning.backend.backend.dto.ObservationRequest;
import com.myga.learning.backend.backend.dto.ObservationResponse;
import com.myga.learning.backend.backend.service.ObservationService;
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
@RequestMapping("/api")
public class ObservationController {

    private final ObservationService observationService;

    public ObservationController(ObservationService observationService) {
        this.observationService = observationService;
    }

    /** Record an observation (ADMIN or a teacher of the student's class). */
    @PostMapping("/observations")
    public ResponseEntity<ObservationResponse> create(@Valid @RequestBody ObservationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(observationService.create(request));
    }

    /** A student's observations. Parents see only those flagged visible. */
    @GetMapping("/students/{studentId}/observations")
    public List<ObservationResponse> getStudentObservations(@PathVariable Long studentId) {
        return observationService.getStudentObservations(studentId);
    }
}
