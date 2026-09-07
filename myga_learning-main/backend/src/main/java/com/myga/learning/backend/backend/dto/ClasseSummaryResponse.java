package com.myga.learning.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Flat view of a class, used when nested inside another resource. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClasseSummaryResponse {
    private Long id;
    private int salle;
}
