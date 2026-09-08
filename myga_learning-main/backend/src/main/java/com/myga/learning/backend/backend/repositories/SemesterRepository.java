package com.myga.learning.backend.backend.repositories;

import com.myga.learning.backend.backend.models.Semester;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SemesterRepository extends JpaRepository<Semester, Long> {

    List<Semester> findByAcademicYear_IdOrderByStartDateAsc(Long academicYearId);
}
