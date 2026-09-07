package com.myga.learning.backend.backend.repositories;

import com.myga.learning.backend.backend.models.Parent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParentRepository extends JpaRepository<Parent, Long> {

    Optional<Parent> findByUser_Email(String email);

    /** Parents linked to a given student. */
    List<Parent> findDistinctByStudents_Id(Long studentId);

    /** Parents who have at least one child in the given class. */
    List<Parent> findDistinctByStudents_Classe_Id(Long classeId);
}
