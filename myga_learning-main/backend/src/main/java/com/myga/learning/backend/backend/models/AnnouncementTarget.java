package com.myga.learning.backend.backend.models;

/** Audience of an announcement. */
public enum AnnouncementTarget {
    /** Everyone. */
    ALL,
    /** All parent accounts. */
    PARENTS,
    /** All teacher accounts. */
    TEACHERS,
    /** Parents of the students in a specific class. */
    CLASS,
    /** A single specific user. */
    USER
}
