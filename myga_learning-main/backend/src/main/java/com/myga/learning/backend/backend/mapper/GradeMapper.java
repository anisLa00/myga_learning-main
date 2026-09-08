package com.myga.learning.backend.backend.mapper;

import com.myga.learning.backend.backend.dto.GradeResponse;
import com.myga.learning.backend.backend.models.Assessment;
import com.myga.learning.backend.backend.models.Grade;
import com.myga.learning.backend.backend.models.Semester;
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
        Assessment assessment = grade.getAssessment();
        Semester semester = grade.getSemester();
        String teacherName = teacher == null ? null
                : ((teacher.getPrenom() == null ? "" : teacher.getPrenom() + " ")
                        + (teacher.getNom() == null ? "" : teacher.getNom())).trim();
        return GradeResponse.builder()
                .id(grade.getId())
                .student(StudentMapper.toSummary(grade.getStudent()))
                .subject(SubjectMapper.toResponse(grade.getSubject()))
                .teacherId(teacher == null ? null : teacher.getId())
                .teacherName(teacherName)
                .assessmentId(assessment == null ? null : assessment.getId())
                .assessmentTitle(assessment == null ? null : assessment.getTitle())
                .semesterId(semester == null ? null : semester.getId())
                .semester(semester == null ? null : semester.getLabel())
                .academicYear(AcademicPeriodMapper.academicYearLabel(semester))
                .value(grade.getValue())
                .maxValue(grade.getMaxValue())
                .comment(grade.getComment())
                .date(grade.getDate())
                .build();
    }
}
