package com.myga.learning.backend.backend.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SubjectRequest {

    @NotBlank(message = "nom is required")
    private String nom;

    private String code;
}
