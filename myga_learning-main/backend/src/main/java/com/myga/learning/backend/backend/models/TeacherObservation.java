package com.myga.learning.backend.backend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import java.time.LocalDate;

/**
 * A note a teacher records about a student. The {@code visibleToParents} flag
 * distinguishes feedback meant for parents from internal notes that must stay
 * private to staff.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class TeacherObservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(optional = false)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    /** Optional subject context. */
    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ObservationType type;

    @Lob
    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private LocalDate date;

    /** When false, only staff (admins and teachers) can see this observation. */
    @Column(nullable = false)
    private boolean visibleToParents = true;
}
