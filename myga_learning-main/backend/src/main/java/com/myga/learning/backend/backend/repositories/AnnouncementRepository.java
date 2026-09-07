package com.myga.learning.backend.backend.repositories;

import com.myga.learning.backend.backend.models.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findAllByOrderByCreatedAtDesc();
}
