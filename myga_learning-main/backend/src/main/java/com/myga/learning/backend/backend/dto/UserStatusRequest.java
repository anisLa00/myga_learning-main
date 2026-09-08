package com.myga.learning.backend.backend.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/** Enables or disables an account. */
@Data
public class UserStatusRequest {

    @NotNull(message = "enabled is required")
    private Boolean enabled;
}
