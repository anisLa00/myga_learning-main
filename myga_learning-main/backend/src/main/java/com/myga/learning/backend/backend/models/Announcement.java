package com.myga.learning.backend.backend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Lob;
import java.time.LocalDateTime;

/** A message published by an administrator to a chosen audience. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnnouncementTarget target;

    /** Set only when target == CLASS. */
    @ManyToOne
    @JoinColumn(name = "target_classe_id")
    private Classe targetClasse;

    /** Set only when target == USER. */
    @ManyToOne
    @JoinColumn(name = "target_user_id")
    private User targetUser;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
