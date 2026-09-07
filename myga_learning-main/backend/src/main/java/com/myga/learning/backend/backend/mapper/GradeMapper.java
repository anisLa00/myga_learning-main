package com.myga.learning.backend.backend.mapper;

import com.myga.learning.backend.backend.dto.GradeResponse;
import com.myga.learning.backend.backend.models.Grade;
import com.myga.learning.backend.backend.models.Teacher;

/** Maps {@link Grade} entities to their API response DTOs. */
public final class GradeMapper {

    private GradeMapper() {
    }

    public static GradeResponse toResponse(Grade grade) {
        if (grade == null) {
            return null;
        }
        Teacher teacher = grade.getTeacher();
        String teacherName = teacher == null ? null
                : ((teacher.getPrenom() == null ? "" : teacher.getPrenom() + " ")
                        + (teacher.getNom() == null ? "" : teacher.getNom())).trim();
        return GradeResponse.builder()
                .id(grade.getId())
                .student(StudentMapper.toSummary(grade.getStudent()))
                .subject(SubjectMapper.toResponse(grade.getSubject()))
                .teacherId(teacher == null ? null : teacher.getId())
                .teacherName(teacherName)
                .value(grade.getValue())
                .maxValue(grade.getMaxValue())
                .comment(grade.getComment())
                .date(grade.getDate())
                .build();
    }
}
