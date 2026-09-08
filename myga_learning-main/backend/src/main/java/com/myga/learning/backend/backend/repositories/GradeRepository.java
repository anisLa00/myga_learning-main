package com.myga.learning.backend.backend.repositories;

import com.myga.learning.backend.backend.models.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface GradeRepository extends JpaRepository<Grade, Long> {

    List<Grade> findByStudent_Id(Long studentId);

    List<Grade> findByStudent_IdAndSubject_Id(Long studentId, Long subjectId);

    // --- dashboard feeds ---

    List<Grade> findTop5ByOrderByDateDescIdDesc();

    List<Grade> findTop5ByTeacher_IdOrderByDateDescIdDesc(Long teacherId);

    List<Grade> findTop5ByStudent_IdInOrderByDateDescIdDesc(Collection<Long> studentIds);
}
