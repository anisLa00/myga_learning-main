package com.myga.learning.backend.backend.controller;

import com.myga.learning.backend.backend.dto.AnnouncementRequest;
import com.myga.learning.backend.backend.dto.AnnouncementResponse;
import com.myga.learning.backend.backend.service.AnnouncementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    /** Publish an announcement (admin only, enforced by security config). */
    @PostMapping
    public ResponseEntity<AnnouncementResponse> create(@Valid @RequestBody AnnouncementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(announcementService.create(request));
    }

    /** All announcements (admin management view). */
    @GetMapping
    public List<AnnouncementResponse> findAll() {
        return announcementService.findAll();
    }

    /** Announcements relevant to the authenticated user. */
    @GetMapping("/me")
    public List<AnnouncementResponse> findMine() {
        return announcementService.findForCurrentUser();
    }
}
