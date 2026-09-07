package com.myga.learning.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Full student view returned by the API, with one level of related data. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {
    private Long id;
    private String nom;
    private String prenom;
    private int age;
    private ClasseSummaryResponse classe;
    private List<ParentSummaryResponse> parents;
}
