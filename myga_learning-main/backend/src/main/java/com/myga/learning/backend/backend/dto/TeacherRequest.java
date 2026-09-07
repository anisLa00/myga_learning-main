package com.myga.learning.backend.backend.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Admin payload to create a teacher together with their login account.
 * There is no public signup, so the account is provisioned here.
 */
@Data
public class TeacherRequest {

    @NotBlank(message = "nom is required")
    private String nom;

    @NotBlank(message = "prenom is required")
    private String prenom;

    @NotBlank(message = "email is required")
    @Email(message = "email must be a valid email address")
    private String email;

    @NotBlank(message = "password is required")
    @Size(min = 6, message = "password must be at least 6 characters")
    private String password;
}
