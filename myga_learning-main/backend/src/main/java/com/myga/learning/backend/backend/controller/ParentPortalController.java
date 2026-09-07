package com.myga.learning.backend.backend.controller;

import com.myga.learning.backend.backend.dto.StudentSummaryResponse;
import com.myga.learning.backend.backend.service.ParentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** The authenticated parent's own view. Never takes a parent id from the client. */
@RestController
@RequestMapping("/api/parent")
public class ParentPortalController {

    private final ParentService parentService;

    public ParentPortalController(ParentService parentService) {
        this.parentService = parentService;
    }

    @GetMapping("/me/children")
    public List<StudentSummaryResponse> myChildren() {
        return parentService.getMyChildren();
    }
}
