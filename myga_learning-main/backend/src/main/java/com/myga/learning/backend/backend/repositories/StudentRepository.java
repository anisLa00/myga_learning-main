package com.myga.learning.backend.backend.repositories;

import com.myga.learning.backend.backend.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    /** Number of students across the given classes (used by the teacher dashboard). */
    long countByClasse_IdIn(Collection<Long> classeIds);
}
