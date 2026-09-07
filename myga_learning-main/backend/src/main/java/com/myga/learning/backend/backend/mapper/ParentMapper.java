package com.myga.learning.backend.backend.mapper;

import com.myga.learning.backend.backend.dto.ParentResponse;
import com.myga.learning.backend.backend.dto.ParentSummaryResponse;
import com.myga.learning.backend.backend.dto.StudentSummaryResponse;
import com.myga.learning.backend.backend.models.Parent;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/** Maps {@link Parent} entities to their API response DTOs. */
public final class ParentMapper {

    private ParentMapper() {
    }

    public static ParentSummaryResponse toSummary(Parent parent) {
        if (parent == null) {
            return null;
        }
        return ParentSummaryResponse.builder()
                .phone(parent.getPhone())
                .nom(parent.getNom())
                .prenom(parent.getPrenom())
                .email(parent.getEmail())
                .build();
    }

    public static ParentResponse toResponse(Parent parent) {
        if (parent == null) {
            return null;
        }
        List<StudentSummaryResponse> students = parent.getStudents() == null
                ? Collections.emptyList()
                : parent.getStudents().stream().map(StudentMapper::toSummary).collect(Collectors.toList());
        return ParentResponse.builder()
                .phone(parent.getPhone())
                .nom(parent.getNom())
                .prenom(parent.getPrenom())
                .email(parent.getEmail())
                .students(students)
                .build();
    }
}
