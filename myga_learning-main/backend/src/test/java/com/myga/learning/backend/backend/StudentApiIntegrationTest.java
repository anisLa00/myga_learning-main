package com.myga.learning.backend.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myga.learning.backend.backend.dto.StudentRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the student API end-to-end (real beans + H2) to verify the new
 * validation and global exception handling: 201 on create, 400 with field
 * errors on invalid input, and 404 (not 500) for a missing resource.
 */
@SpringBootTest
@AutoConfigureMockMvc
class StudentApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createValidStudentReturns201() throws Exception {
        StudentRequest request = new StudentRequest();
        request.setNom("Dupont");
        request.setPrenom("Marie");
        request.setAge(15);

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nom").value("Dupont"))
                .andExpect(jsonPath("$.parents").isArray());
    }

    @Test
    void createInvalidStudentReturns400WithFieldErrors() throws Exception {
        // Blank names and age 0 all violate the request constraints.
        StudentRequest request = new StudentRequest();

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.nom").exists())
                .andExpect(jsonPath("$.fieldErrors.prenom").exists());
    }

    @Test
    void getMissingStudentReturns404() throws Exception {
        mockMvc.perform(get("/api/students/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }
}
