package com.myga.learning.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemesterResponse {
    private Long id;
    private String label;
    private Long academicYearId;
    /** Derived from the semester's academic year. */
    private String academicYear;
    private LocalDate startDate;
    private LocalDate endDate;
}
