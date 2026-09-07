package com.myga.learning.backend.backend.mapper;

import com.myga.learning.backend.backend.dto.ClasseSummaryResponse;
import com.myga.learning.backend.backend.dto.SubjectResponse;
import com.myga.learning.backend.backend.dto.TeacherResponse;
import com.myga.learning.backend.backend.models.Teacher;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/** Maps {@link Teacher} entities to their API response DTOs. */
public final class TeacherMapper {

    private TeacherMapper() {
    }

    public static TeacherResponse toResponse(Teacher teacher) {
        if (teacher == null) {
            return null;
        }
        List<SubjectResponse> subjects = teacher.getSubjects() == null
                ? Collections.emptyList()
                : teacher.getSubjects().stream().map(SubjectMapper::toResponse).collect(Collectors.toList());
        List<ClasseSummaryResponse> classes = teacher.getClasses() == null
                ? Collections.emptyList()
                : teacher.getClasses().stream().map(ClasseMapper::toSummary).collect(Collectors.toList());
        return TeacherResponse.builder()
                .id(teacher.getId())
                .nom(teacher.getNom())
                .prenom(teacher.getPrenom())
                .email(teacher.getUser() == null ? null : teacher.getUser().getEmail())
                .subjects(subjects)
                .classes(classes)
                .build();
    }
}
