package com.myga.learning.backend.backend.repositories;

import com.myga.learning.backend.backend.models.Attendance;
import com.myga.learning.backend.backend.models.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByStudent_IdOrderByDateDesc(Long studentId);

    List<Attendance> findByClasse_IdAndDate(Long classeId, LocalDate date);

    // --- dashboard feeds ---

    List<Attendance> findTop5ByOrderByDateDescIdDesc();

    List<Attendance> findTop5ByStatusOrderByDateDescIdDesc(AttendanceStatus status);

    List<Attendance> findTop5ByStudent_IdInAndStatusOrderByDateDescIdDesc(
            Collection<Long> studentIds, AttendanceStatus status);
}
