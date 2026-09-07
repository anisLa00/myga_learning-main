package com.myga.learning.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Full class view returned by the API, with its students summarised. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClasseResponse {
    private Long id;
    private int salle;
    private List<StudentSummaryResponse> students;
}
