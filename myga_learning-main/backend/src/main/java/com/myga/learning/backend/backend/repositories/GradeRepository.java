package com.myga.learning.backend.backend.repositories;

import com.myga.learning.backend.backend.models.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GradeRepository extends JpaRepository<Grade, Long> {

    List<Grade> findByStudent_Id(Long studentId);

    List<Grade> findByStudent_IdAndSubject_Id(Long studentId, Long subjectId);
}
