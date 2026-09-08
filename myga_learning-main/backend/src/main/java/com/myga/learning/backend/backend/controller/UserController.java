package com.myga.learning.backend.backend.controller;

import com.myga.learning.backend.backend.dto.UserResponse;
import com.myga.learning.backend.backend.dto.UserStatusRequest;
import com.myga.learning.backend.backend.models.Role;
import com.myga.learning.backend.backend.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/** Account administration. Restricted to ADMIN in the security config. */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> findAll(@RequestParam(required = false) Role role) {
        return userService.findAll(role);
    }

    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    /** Enable or disable an account without deleting it. */
    @PutMapping("/{id}/status")
    public UserResponse setStatus(@PathVariable Long id, @Valid @RequestBody UserStatusRequest request) {
        return userService.setEnabled(id, request.getEnabled());
    }
}
