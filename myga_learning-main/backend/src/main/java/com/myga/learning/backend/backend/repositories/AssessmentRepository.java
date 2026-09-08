package com.myga.learning.backend.backend.repositories;

import com.myga.learning.backend.backend.models.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    List<Assessment> findByClasse_IdOrderByDateDesc(Long classeId);

    List<Assessment> findByTeacher_IdOrderByDateDesc(Long teacherId);

    /** Upcoming assessments for a class, soonest first. */
    List<Assessment> findByClasse_IdAndDateGreaterThanEqualOrderByDateAsc(Long classeId, LocalDate from);

    /** Upcoming assessments across a teacher's own assessments. */
    List<Assessment> findTop5ByTeacher_IdAndDateGreaterThanEqualOrderByDateAsc(Long teacherId, LocalDate from);
}
