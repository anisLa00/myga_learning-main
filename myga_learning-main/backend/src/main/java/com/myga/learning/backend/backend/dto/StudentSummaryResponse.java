package com.myga.learning.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Flat view of a student, used when nested inside another resource. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentSummaryResponse {
    private Long id;
    private String nom;
    private String prenom;
    private int age;
}
