package com.myga.learning.backend.backend.dto;

import com.myga.learning.backend.backend.models.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Account view for administrators. The password hash is never exposed. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String nom;
    private String prenom;
    private Role role;
    private boolean enabled;
}
