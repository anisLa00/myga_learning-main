package com.myga.learning.backend.backend.repositories;

import com.myga.learning.backend.backend.models.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    Optional<Teacher> findByUser_Email(String email);
}
