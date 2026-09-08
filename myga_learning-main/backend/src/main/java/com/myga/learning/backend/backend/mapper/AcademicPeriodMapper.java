package com.myga.learning.backend.backend.mapper;

import com.myga.learning.backend.backend.dto.AcademicYearResponse;
import com.myga.learning.backend.backend.dto.SemesterResponse;
import com.myga.learning.backend.backend.models.AcademicYear;
import com.myga.learning.backend.backend.models.Semester;

/** Maps academic-period entities to their API response DTOs. */
public final class AcademicPeriodMapper {

    private AcademicPeriodMapper() {
    }

    public static AcademicYearResponse toResponse(AcademicYear year) {
        if (year == null) {
            return null;
        }
        return AcademicYearResponse.builder()
                .id(year.getId())
                .label(year.getLabel())
                .startDate(year.getStartDate())
                .endDate(year.getEndDate())
                .current(year.isCurrent())
                .build();
    }

    public static SemesterResponse toResponse(Semester semester) {
        if (semester == null) {
            return null;
        }
        AcademicYear year = semester.getAcademicYear();
        return SemesterResponse.builder()
                .id(semester.getId())
                .label(semester.getLabel())
                .academicYearId(year == null ? null : year.getId())
                .academicYear(year == null ? null : year.getLabel())
                .startDate(semester.getStartDate())
                .endDate(semester.getEndDate())
                .build();
    }

    /** Label of the academic year a semester belongs to, or null. */
    public static String academicYearLabel(Semester semester) {
        if (semester == null || semester.getAcademicYear() == null) {
            return null;
        }
        return semester.getAcademicYear().getLabel();
    }
}
