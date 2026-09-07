package com.myga.learning.backend.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myga.learning.backend.backend.dto.LoginRequest;
import com.myga.learning.backend.backend.dto.StudentRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies authentication and role-based authorization end-to-end using the
 * seeded default admin: login succeeds and returns a JWT, bad credentials are
 * rejected with 401, protected endpoints require a token, and a valid admin
 * token grants access.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String loginAsAdmin() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail("admin@myga.local");
        login.setPassword("admin123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("accessToken").asText();
    }

    @Test
    void loginWithValidAdminCredentialsReturnsToken() throws Exception {
        loginAsAdmin();
    }

    @Test
    void loginWithBadPasswordReturns401() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail("admin@myga.local");
        login.setPassword("wrong-password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointWithAdminTokenReturns200() throws Exception {
        String token = loginAsAdmin();
        mockMvc.perform(get("/api/students").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void writeEndpointWithAdminTokenReturns201() throws Exception {
        String token = loginAsAdmin();
        StudentRequest request = new StudentRequest();
        request.setNom("Martin");
        request.setPrenom("Paul");
        request.setAge(12);

        mockMvc.perform(post("/api/students")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
