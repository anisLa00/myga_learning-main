package com.myga.learning.backend.backend.mapper;

import com.myga.learning.backend.backend.dto.ParentSummaryResponse;
import com.myga.learning.backend.backend.dto.StudentResponse;
import com.myga.learning.backend.backend.dto.StudentSummaryResponse;
import com.myga.learning.backend.backend.models.Student;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/** Maps {@link Student} entities to their API response DTOs. */
public final class StudentMapper {

    private StudentMapper() {
    }

    public static StudentSummaryResponse toSummary(Student student) {
        if (student == null) {
            return null;
        }
        return StudentSummaryResponse.builder()
                .id(student.getId())
                .nom(student.getNom())
                .prenom(student.getPrenom())
                .age(student.getAge())
                .build();
    }

    public static StudentResponse toResponse(Student student) {
        if (student == null) {
            return null;
        }
        List<ParentSummaryResponse> parents = student.getParents() == null
                ? Collections.emptyList()
                : student.getParents().stream().map(ParentMapper::toSummary).collect(Collectors.toList());
        return StudentResponse.builder()
                .id(student.getId())
                .nom(student.getNom())
                .prenom(student.getPrenom())
                .age(student.getAge())
                .classe(ClasseMapper.toSummary(student.getClasse()))
                .parents(parents)
                .build();
    }
}
