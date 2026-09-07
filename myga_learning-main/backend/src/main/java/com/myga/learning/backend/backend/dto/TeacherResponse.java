package com.myga.learning.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherResponse {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private List<SubjectResponse> subjects;
    private List<ClasseSummaryResponse> classes;
}
