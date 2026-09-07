package com.myga.learning.backend.backend.models;

/**
 * Application roles. Students are academic entities and never authenticate,
 * so there is deliberately no STUDENT role. Each role maps to a Spring
 * Security authority of the form {@code ROLE_<NAME>}.
 */
public enum Role {
    ADMIN,
    TEACHER,
    PARENT;

    /** Spring Security authority name for this role. */
    public String authority() {
        return "ROLE_" + name();
    }
}
