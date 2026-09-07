package com.myga.learning.backend.backend.mapper;

import com.myga.learning.backend.backend.dto.SubjectResponse;
import com.myga.learning.backend.backend.models.Subject;

/** Maps {@link Subject} entities to their API response DTOs. */
public final class SubjectMapper {

    private SubjectMapper() {
    }

    public static SubjectResponse toResponse(Subject subject) {
        if (subject == null) {
            return null;
        }
        return SubjectResponse.builder()
                .id(subject.getId())
                .nom(subject.getNom())
                .code(subject.getCode())
                .build();
    }
}
