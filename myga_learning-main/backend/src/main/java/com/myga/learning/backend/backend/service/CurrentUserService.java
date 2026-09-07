package com.myga.learning.backend.backend.service;

import com.myga.learning.backend.backend.models.Parent;
import com.myga.learning.backend.backend.models.Teacher;
import com.myga.learning.backend.backend.models.User;
import com.myga.learning.backend.backend.repositories.ParentRepository;
import com.myga.learning.backend.backend.repositories.TeacherRepository;
import com.myga.learning.backend.backend.repositories.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Resolves the currently authenticated principal to the domain records that
 * drive ownership checks. The username in the security context is the user's
 * email (see the JWT setup), which we trust because it came from a validated
 * token, never from a request parameter.
 */
@Service
public class CurrentUserService {

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final ParentRepository parentRepository;

    public CurrentUserService(UserRepository userRepository,
                              TeacherRepository teacherRepository,
                              ParentRepository parentRepository) {
        this.userRepository = userRepository;
        this.teacherRepository = teacherRepository;
        this.parentRepository = parentRepository;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("No authenticated user");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
    }

    public Teacher getCurrentTeacher() {
        String email = getCurrentUser().getEmail();
        return teacherRepository.findByUser_Email(email)
                .orElseThrow(() -> new AccessDeniedException("No teacher profile for the current user"));
    }

    public Parent getCurrentParent() {
        String email = getCurrentUser().getEmail();
        return parentRepository.findByUser_Email(email)
                .orElseThrow(() -> new AccessDeniedException("No parent profile for the current user"));
    }

    public boolean hasRole(String role) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }
}
