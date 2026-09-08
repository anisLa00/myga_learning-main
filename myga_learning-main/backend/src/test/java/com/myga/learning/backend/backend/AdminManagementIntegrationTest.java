package com.myga.learning.backend.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myga.learning.backend.backend.dto.UserStatusRequest;
import com.myga.learning.backend.backend.models.Role;
import com.myga.learning.backend.backend.models.User;
import com.myga.learning.backend.backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Account administration: listing, enabling/disabling, and its restrictions. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminManagementIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Long teacherUserId;
    private Long adminUserId;

    @BeforeEach
    void seed() {
        User teacher = new User();
        teacher.setEmail("admin.mgmt.teacher@test.local");
        teacher.setPassword(passwordEncoder.encode("password123"));
        teacher.setRole(Role.TEACHER);
        teacher.setEnabled(true);
        teacherUserId = userRepository.save(teacher).getId();

        adminUserId = userRepository.findByEmail("admin@myga.local")
                .orElseThrow(() -> new IllegalStateException("seeded admin missing"))
                .getId();
    }

    @Test
    @WithMockUser(username = "admin@myga.local", roles = "ADMIN")
    void adminCanListUsersAndFilterByRole() throws Exception {
        mockMvc.perform(get("/api/users?role=TEACHER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("TEACHER"));
    }

    @Test
    @WithMockUser(username = "admin@myga.local", roles = "ADMIN")
    void adminCanDisableAndReEnableAnAccount() throws Exception {
        mockMvc.perform(put("/api/users/" + teacherUserId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest(false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));

        mockMvc.perform(put("/api/users/" + teacherUserId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest(true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @WithMockUser(username = "admin@myga.local", roles = "ADMIN")
    void adminCannotDisableTheirOwnAccount() throws Exception {
        mockMvc.perform(put("/api/users/" + adminUserId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest(false))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin.mgmt.teacher@test.local", roles = "TEACHER")
    void nonAdminCannotListUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    private UserStatusRequest statusRequest(boolean enabled) {
        UserStatusRequest r = new UserStatusRequest();
        r.setEnabled(enabled);
        return r;
    }
}
