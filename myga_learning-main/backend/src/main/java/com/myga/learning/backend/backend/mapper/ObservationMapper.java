package com.myga.learning.backend.backend.mapper;

import com.myga.learning.backend.backend.dto.ObservationResponse;
import com.myga.learning.backend.backend.models.Teacher;
import com.myga.learning.backend.backend.models.TeacherObservation;

/** Maps {@link TeacherObservation} entities to their API response DTOs. */
public final class ObservationMapper {

    private ObservationMapper() {
    }

    public static ObservationResponse toResponse(TeacherObservation o) {
        if (o == null) {
            return null;
        }
        Teacher teacher = o.getTeacher();
        String teacherName = teacher == null ? null
                : ((teacher.getPrenom() == null ? "" : teacher.getPrenom() + " ")
                        + (teacher.getNom() == null ? "" : teacher.getNom())).trim();
        return ObservationResponse.builder()
                .id(o.getId())
                .student(StudentMapper.toSummary(o.getStudent()))
                .teacherId(teacher == null ? null : teacher.getId())
                .teacherName(teacherName)
                .subject(SubjectMapper.toResponse(o.getSubject()))
                .type(o.getType())
                .message(o.getMessage())
                .date(o.getDate())
                .visibleToParents(o.isVisibleToParents())
                .build();
    }
}
