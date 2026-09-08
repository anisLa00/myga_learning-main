package com.myga.learning.backend.backend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;

/** A school year, e.g. "2025-2026". */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class AcademicYear {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String label;

    private LocalDate startDate;
    private LocalDate endDate;

    /** Marks the year currently in progress; at most one should be true. */
    @Column(nullable = false)
    private boolean current = false;
}
