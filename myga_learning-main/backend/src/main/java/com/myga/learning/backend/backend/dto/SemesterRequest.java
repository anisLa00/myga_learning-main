package com.myga.learning.backend.backend.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class SemesterRequest {

    @NotBlank(message = "label is required")
    private String label;

    @NotNull(message = "academicYearId is required")
    private Long academicYearId;

    private LocalDate startDate;
    private LocalDate endDate;
}
