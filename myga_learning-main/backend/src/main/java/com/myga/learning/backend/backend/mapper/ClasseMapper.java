package com.myga.learning.backend.backend.mapper;

import com.myga.learning.backend.backend.dto.ClasseResponse;
import com.myga.learning.backend.backend.dto.ClasseSummaryResponse;
import com.myga.learning.backend.backend.dto.StudentSummaryResponse;
import com.myga.learning.backend.backend.models.Classe;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/** Maps {@link Classe} entities to their API response DTOs. */
public final class ClasseMapper {

    private ClasseMapper() {
    }

    public static ClasseSummaryResponse toSummary(Classe classe) {
        if (classe == null) {
            return null;
        }
        return ClasseSummaryResponse.builder()
                .id(classe.getId())
                .salle(classe.getSalle())
                .build();
    }

    public static ClasseResponse toResponse(Classe classe) {
        if (classe == null) {
            return null;
        }
        List<StudentSummaryResponse> students = classe.getStudents() == null
                ? Collections.emptyList()
                : classe.getStudents().stream().map(StudentMapper::toSummary).collect(Collectors.toList());
        return ClasseResponse.builder()
                .id(classe.getId())
                .salle(classe.getSalle())
                .students(students)
                .build();
    }
}
