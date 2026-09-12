package com.myga.learning.backend.backend.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/** A signed-in user changing their own password. */
@Data
public class ChangePasswordRequest {

    @NotBlank(message = "currentPassword is required")
    private String currentPassword;

    @NotBlank(message = "newPassword is required")
    @Size(min = 8, message = "newPassword must be at least 8 characters")
    private String newPassword;
}
