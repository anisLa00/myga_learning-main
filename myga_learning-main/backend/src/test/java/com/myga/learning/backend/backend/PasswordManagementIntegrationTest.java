package com.myga.learning.backend.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myga.learning.backend.backend.dto.ChangePasswordRequest;
import com.myga.learning.backend.backend.dto.LoginRequest;
import com.myga.learning.backend.backend.dto.ResetPasswordRequest;
import com.myga.learning.backend.backend.dto.TeacherRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Password management: a user changes their own password (proving they know the
 * current one), an administrator resets someone else's, and in both cases the
 * tokens issued before the change stop working.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PasswordManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String login(String email, String password) throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail(email);
        login.setPassword(password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private String adminToken() throws Exception {
        return login("admin@myga.local", "admin123");
    }

    /** Creates a teacher login and returns its user id. */
    private long createTeacher(String adminToken, String email) throws Exception {
        TeacherRequest request = new TeacherRequest();
        request.setNom("Hopper");
        request.setPrenom("Grace");
        request.setEmail(email);
        request.setPassword("teacher123");
        mockMvc.perform(post("/api/teachers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        MvcResult users = mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        for (JsonNode user : objectMapper.readTree(users.getResponse().getContentAsString())) {
            if (email.equals(user.get("email").asText())) {
                return user.get("id").asLong();
            }
        }
        throw new AssertionError("No account created for " + email);
    }

    private String changePasswordBody(String current, String next) throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword(current);
        request.setNewPassword(next);
        return objectMapper.writeValueAsString(request);
    }

    private String resetPasswordBody(String next) throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setNewPassword(next);
        return objectMapper.writeValueAsString(request);
    }

    @Test
    void changingOwnPasswordWorksAndTheOldPasswordStopsWorking() throws Exception {
        String email = "change-ok@myga.local";
        createTeacher(adminToken(), email);
        String token = login(email, "teacher123");

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(changePasswordBody("teacher123", "brand-new-pass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());

        // The new password authenticates; the old one no longer does.
        login(email, "brand-new-pass");

        LoginRequest old = new LoginRequest();
        old.setEmail(email);
        old.setPassword("teacher123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(old)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokensIssuedBeforeAPasswordChangeAreRejected() throws Exception {
        String email = "old-token@myga.local";
        createTeacher(adminToken(), email);
        String oldToken = login(email, "teacher123");

        // The old token works right now.
        mockMvc.perform(get("/api/teacher/me/classes").header("Authorization", "Bearer " + oldToken))
                .andExpect(status().isOk());

        MvcResult changed = mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + oldToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(changePasswordBody("teacher123", "another-secret")))
                .andExpect(status().isOk())
                .andReturn();

        // ... and is refused afterwards, while the freshly issued one works.
        mockMvc.perform(get("/api/teacher/me/classes").header("Authorization", "Bearer " + oldToken))
                .andExpect(status().isUnauthorized());

        String newToken = objectMapper.readTree(changed.getResponse().getContentAsString())
                .get("accessToken").asText();
        mockMvc.perform(get("/api/teacher/me/classes").header("Authorization", "Bearer " + newToken))
                .andExpect(status().isOk());
    }

    @Test
    void changingPasswordWithTheWrongCurrentPasswordIsRejected() throws Exception {
        String email = "wrong-current@myga.local";
        createTeacher(adminToken(), email);
        String token = login(email, "teacher123");

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(changePasswordBody("not-my-password", "irrelevant-value")))
                .andExpect(status().isUnauthorized());

        // The password is unchanged.
        login(email, "teacher123");
    }

    @Test
    void changePasswordRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(changePasswordBody("teacher123", "brand-new-pass")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void aShortNewPasswordIsRejected() throws Exception {
        String email = "short-pass@myga.local";
        createTeacher(adminToken(), email);
        String token = login(email, "teacher123");

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(changePasswordBody("teacher123", "short")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.newPassword").exists());
    }

    @Test
    void reusingTheCurrentPasswordIsRejected() throws Exception {
        String email = "same-pass@myga.local";
        createTeacher(adminToken(), email);
        String token = login(email, "teacher123");

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(changePasswordBody("teacher123", "teacher123")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void anAdminCanResetAnotherAccountsPassword() throws Exception {
        String admin = adminToken();
        String email = "reset-me@myga.local";
        long userId = createTeacher(admin, email);
        String staleToken = login(email, "teacher123");

        mockMvc.perform(put("/api/users/" + userId + "/password")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resetPasswordBody("admin-set-pass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));

        login(email, "admin-set-pass");

        // The session opened with the old password is over.
        mockMvc.perform(get("/api/teacher/me/classes").header("Authorization", "Bearer " + staleToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void aTeacherCannotResetSomeoneElsesPassword() throws Exception {
        String admin = adminToken();
        long victimId = createTeacher(admin, "victim@myga.local");
        createTeacher(admin, "attacker@myga.local");
        String attacker = login("attacker@myga.local", "teacher123");

        mockMvc.perform(put("/api/users/" + victimId + "/password")
                        .header("Authorization", "Bearer " + attacker)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resetPasswordBody("hijacked-pass")))
                .andExpect(status().isForbidden());

        // The victim's password is untouched.
        login("victim@myga.local", "teacher123");
    }
}
