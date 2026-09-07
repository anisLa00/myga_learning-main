package com.myga.learning.backend.backend.repositories;

import com.myga.learning.backend.backend.models.Parent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParentRepository extends JpaRepository<Parent, Long> {

    Optional<Parent> findByUser_Email(String email);
}
