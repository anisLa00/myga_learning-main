package com.myga.learning.backend.backend.mapper;

import com.myga.learning.backend.backend.dto.AssessmentResponse;
import com.myga.learning.backend.backend.models.Assessment;
import com.myga.learning.backend.backend.models.Semester;
import com.myga.learning.backend.backend.models.Teacher;

/** Maps {@link Assessment} entities to their API response DTOs. */
public final class AssessmentMapper {

    private AssessmentMapper() {
    }

    public static AssessmentResponse toResponse(Assessment a) {
        if (a == null) {
            return null;
        }
        Teacher teacher = a.getTeacher();
        Semester semester = a.getSemester();
        String teacherName = teacher == null ? null
                : ((teacher.getPrenom() == null ? "" : teacher.getPrenom() + " ")
                        + (teacher.getNom() == null ? "" : teacher.getNom())).trim();
        return AssessmentResponse.builder()
                .id(a.getId())
                .title(a.getTitle())
                .description(a.getDescription())
                .subject(SubjectMapper.toResponse(a.getSubject()))
                .classe(ClasseMapper.toSummary(a.getClasse()))
                .teacherId(teacher == null ? null : teacher.getId())
                .teacherName(teacherName)
                .type(a.getType())
                .date(a.getDate())
                .maxGrade(a.getMaxGrade())
                .semesterId(semester == null ? null : semester.getId())
                .semester(semester == null ? null : semester.getLabel())
                .academicYear(AcademicPeriodMapper.academicYearLabel(semester))
                .build();
    }
}
