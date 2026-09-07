package com.myga.learning.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Flat view of a parent, used when nested inside another resource. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentSummaryResponse {
    private Long phone;
    private String nom;
    private String prenom;
    private String email;
}
