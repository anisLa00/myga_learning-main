package com.myga.learning.backend.backend.service;

import com.myga.learning.backend.backend.dto.ChangePasswordRequest;
import com.myga.learning.backend.backend.dto.LoginRequest;
import com.myga.learning.backend.backend.dto.LoginResponse;
import com.myga.learning.backend.backend.models.User;
import com.myga.learning.backend.backend.repositories.UserRepository;
import com.myga.learning.backend.backend.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Authenticates credentials and issues a JWT. */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       UserRepository userRepository,
                       CurrentUserService currentUserService,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        // Throws AuthenticationException (bad credentials / disabled) on failure,
        // which GlobalExceptionHandler maps to 401.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("No account for email " + request.getEmail()));

        return issueToken(user);
    }

    /**
     * Changes the password of the caller's own account. The current password
     * must be supplied and must match, so a stolen token alone cannot be used
     * to take an account over. Every token issued before this call stops
     * working, so a fresh one is returned to keep the caller signed in.
     */
    @Transactional
    public LoginResponse changePassword(ChangePasswordRequest request) {
        User user = currentUserService.getCurrentUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("The new password must differ from the current one");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordVersion(user.currentPasswordVersion() + 1);
        userRepository.save(user);

        return issueToken(user);
    }

    private LoginResponse issueToken(User user) {
        UserDetails principal = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRole().authority())
                .build();

        String token = jwtService.generateToken(principal, user.getRole().name(), user.currentPasswordVersion());

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .email(user.getEmail())
                .role(user.getRole().name())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .build();
    }
}
