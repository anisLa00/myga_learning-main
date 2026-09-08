package com.myga.learning.backend.backend.service;

import com.myga.learning.backend.backend.dto.UserResponse;
import com.myga.learning.backend.backend.exception.ResourceNotFoundException;
import com.myga.learning.backend.backend.models.Role;
import com.myga.learning.backend.backend.models.User;
import com.myga.learning.backend.backend.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/** Account administration: listing and enabling/disabling logins. */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public UserService(UserRepository userRepository, CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
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
