package com.myga.learning.backend.backend.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
public class AcademicYearRequest {

    @NotBlank(message = "label is required")
    private String label;

    private LocalDate startDate;
    private LocalDate endDate;

    /** When true, this becomes the current year and any other is cleared. */
    private Boolean current;
}
