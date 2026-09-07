package com.myga.learning.backend.backend.mapper;

import com.myga.learning.backend.backend.dto.AttendanceResponse;
import com.myga.learning.backend.backend.models.Attendance;
import com.myga.learning.backend.backend.models.Teacher;

/** Maps {@link Attendance} entities to their API response DTOs. */
public final class AttendanceMapper {

    private AttendanceMapper() {
    }

    public static AttendanceResponse toResponse(Attendance attendance) {
        if (attendance == null) {
            return null;
        }
        Teacher teacher = attendance.getTeacher();
        String teacherName = teacher == null ? null
                : ((teacher.getPrenom() == null ? "" : teacher.getPrenom() + " ")
                        + (teacher.getNom() == null ? "" : teacher.getNom())).trim();
        return AttendanceResponse.builder()
                .id(attendance.getId())
                .student(StudentMapper.toSummary(attendance.getStudent()))
                .classe(ClasseMapper.toSummary(attendance.getClasse()))
                .subject(SubjectMapper.toResponse(attendance.getSubject()))
                .teacherId(teacher == null ? null : teacher.getId())
                .teacherName(teacherName)
                .date(attendance.getDate())
                .status(attendance.getStatus())
                .note(attendance.getNote())
                .build();
    }
}
