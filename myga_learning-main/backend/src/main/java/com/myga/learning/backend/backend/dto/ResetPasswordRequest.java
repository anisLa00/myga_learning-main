package com.myga.learning.backend.backend.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * An administrator setting a new password for someone else's account — the
 * way a locked-out teacher or parent gets back in, since there is no public
 * password-reset flow by design.
 */
@Data
public class ResetPasswordRequest {

    @NotBlank(message = "newPassword is required")
    @Size(min = 8, message = "newPassword must be at least 8 characters")
    private String newPassword;
}
