package com.myga.learning.backend.backend.repositories;

import com.myga.learning.backend.backend.models.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
}
