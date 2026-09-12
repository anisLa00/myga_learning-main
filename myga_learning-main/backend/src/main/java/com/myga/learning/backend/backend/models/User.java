package com.myga.learning.backend.backend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * An authenticating account. Only ADMIN, TEACHER and PARENT users exist;
 * accounts are created by an administrator (there is no public signup).
 * The email address doubles as the login username.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    /** BCrypt-hashed password. Never stored or returned in plain text. */
    @Column(nullable = false)
    private String password;

    private String nom;
    private String prenom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /** Lets an administrator disable/enable an account without deleting it. */
    @Column(nullable = false)
    private boolean enabled = true;

    /**
     * Bumped every time the password changes. Each token carries the version it
     * was issued under, so changing (or resetting) a password instantly ends
     * every session opened with the old one. A timestamp cannot do this job:
     * the JWT issued-at claim only has one-second resolution, so a token minted
     * in the same second as the change would slip through.
     * <p>Null on rows created before this column existed; treated as 0.
     */
    private Integer passwordVersion;

    /** Null-safe view of {@link #passwordVersion}. */
    public int currentPasswordVersion() {
        return passwordVersion == null ? 0 : passwordVersion;
    }
}
