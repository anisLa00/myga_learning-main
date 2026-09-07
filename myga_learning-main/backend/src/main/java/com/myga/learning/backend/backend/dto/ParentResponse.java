package com.myga.learning.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Full parent view returned by the API, with one level of related data. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentResponse {
    private Long phone;
    private String nom;
    private String prenom;
    private String email;
    private List<StudentSummaryResponse> students;
}
