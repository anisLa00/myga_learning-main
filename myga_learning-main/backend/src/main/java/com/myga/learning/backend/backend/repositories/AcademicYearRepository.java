package com.myga.learning.backend.backend.repositories;

import com.myga.learning.backend.backend.models.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {

    boolean existsByLabel(String label);

    Optional<AcademicYear> findByCurrentTrue();

    List<AcademicYear> findAllByCurrentTrue();
}
