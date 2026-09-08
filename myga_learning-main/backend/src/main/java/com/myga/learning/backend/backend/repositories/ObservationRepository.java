package com.myga.learning.backend.backend.repositories;

import com.myga.learning.backend.backend.models.TeacherObservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ObservationRepository extends JpaRepository<TeacherObservation, Long> {

    List<TeacherObservation> findByStudent_IdOrderByDateDesc(Long studentId);
}
