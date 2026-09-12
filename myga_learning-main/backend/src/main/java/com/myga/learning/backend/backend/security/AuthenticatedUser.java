package com.myga.learning.backend.backend.security;

import com.myga.learning.backend.backend.models.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

/**
 * The Spring Security principal, carrying the one extra fact the framework's
 * own {@code User} cannot hold: the account's current password version.
 * {@link JwtService} uses it to reject tokens issued under an older one.
 */
public class AuthenticatedUser extends org.springframework.security.core.userdetails.User {

    private final int passwordVersion;

    public AuthenticatedUser(User user) {
        super(user.getEmail(),
              user.getPassword(),
              user.isEnabled(),
              true,   // account non-expired
              true,   // credentials non-expired
              true,   // account non-locked
              Collections.singletonList(new SimpleGrantedAuthority(user.getRole().authority())));
        this.passwordVersion = user.currentPasswordVersion();
    }

    /** 0 for an account whose password has never been changed since creation. */
    public int getPasswordVersion() {
        return passwordVersion;
    }
}
