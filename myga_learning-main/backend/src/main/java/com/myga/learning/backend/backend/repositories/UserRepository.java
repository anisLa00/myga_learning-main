package com.myga.learning.backend.backend.repositories;

import com.myga.learning.backend.backend.models.Role;
import com.myga.learning.backend.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);
}
