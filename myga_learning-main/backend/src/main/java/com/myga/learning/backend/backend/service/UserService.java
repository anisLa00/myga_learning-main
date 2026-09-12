package com.myga.learning.backend.backend.service;

import com.myga.learning.backend.backend.dto.UserResponse;
import com.myga.learning.backend.backend.exception.ResourceNotFoundException;
import com.myga.learning.backend.backend.models.Role;
import com.myga.learning.backend.backend.models.User;
import com.myga.learning.backend.backend.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/** Account administration: listing logins, enabling/disabling, resetting passwords. */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       CurrentUserService currentUserService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll(Role role) {
        List<User> users = role == null ? userRepository.findAll() : userRepository.findByRole(role);
        return users.stream().map(UserService::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return toResponse(getUserOrThrow(id));
    }

    /**
     * Enables or disables an account. An administrator cannot disable their own
     * login, which would otherwise be an easy way to lock everyone out.
     */
    public UserResponse setEnabled(Long id, boolean enabled) {
        User user = getUserOrThrow(id);
        if (!enabled && user.getId().equals(currentUserService.getCurrentUser().getId())) {
            throw new IllegalArgumentException("You cannot disable your own account");
        }
        user.setEnabled(enabled);
        return toResponse(userRepository.save(user));
    }

    /**
     * Sets a new password for an account on an administrator's behalf — the
     * recovery path for a locked-out teacher or parent. Any session opened
     * with the old password stops working immediately.
     */
    public UserResponse resetPassword(Long id, String newPassword) {
        User user = getUserOrThrow(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordVersion(user.currentPasswordVersion() + 1);
        return toResponse(userRepository.save(user));
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("User", id));
    }

    private static UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .build();
    }
}
