package com.myga.learning.backend.backend.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

/**
 * Incoming payload for creating or updating a {@code Student}.
 * Kept separate from the JPA entity so the API contract is explicit and
 * validated before it ever reaches the persistence layer.
 */
@Data
public class StudentRequest {

    @NotBlank(message = "nom is required")
    private String nom;

    @NotBlank(message = "prenom is required")
    private String prenom;

    @Min(value = 1, message = "age must be greater than 0")
    @Max(value = 120, message = "age is not valid")
    private int age;

    /** Optional: assign the student to an existing class by id. */
    private Long classeId;
}
