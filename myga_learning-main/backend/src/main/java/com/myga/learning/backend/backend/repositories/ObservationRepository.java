package com.myga.learning.backend.backend.repositories;

import com.myga.learning.backend.backend.models.TeacherObservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ObservationRepository extends JpaRepository<TeacherObservation, Long> {

    List<TeacherObservation> findByStudent_IdOrderByDateDesc(Long studentId);

    // --- dashboard feeds ---

    List<TeacherObservation> findTop5ByTeacher_IdOrderByDateDescIdDesc(Long teacherId);

    /** Only parent-visible feedback, for the parent dashboard. */
    List<TeacherObservation> findTop5ByStudent_IdInAndVisibleToParentsTrueOrderByDateDescIdDesc(
            Collection<Long> studentIds);
}
