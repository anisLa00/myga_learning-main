package com.myga.learning.backend.backend.dto;

import lombok.Data;

import javax.validation.constraints.Positive;

@Data
public class ClasseRequest {

    @Positive(message = "salle (room number) must be positive")
    private int salle;
}
