package com.myga.learning.backend.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myga.learning.backend.backend.dto.ObservationRequest;
import com.myga.learning.backend.backend.models.Classe;
import com.myga.learning.backend.backend.models.ObservationType;
import com.myga.learning.backend.backend.models.Parent;
import com.myga.learning.backend.backend.models.Role;
import com.myga.learning.backend.backend.models.Student;
import com.myga.learning.backend.backend.models.Teacher;
import com.myga.learning.backend.backend.models.TeacherObservation;
import com.myga.learning.backend.backend.models.User;
import com.myga.learning.backend.backend.repositories.ClasseRepository;
import com.myga.learning.backend.backend.repositories.ObservationRepository;
import com.myga.learning.backend.backend.repositories.ParentRepository;
import com.myga.learning.backend.backend.repositories.StudentRepository;
import com.myga.learning.backend.backend.repositories.TeacherRepository;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ObservationIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private ParentRepository parentRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private ClasseRepository classeRepository;
    @Autowired private TeacherRepository teacherRepository;
    @Autowired private ObservationRepository observationRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Long studentAId;
    private Long studentBId;

    @BeforeEach
    void seed() {
        Classe classe1 = classeRepository.save(newClasse(501));
        Classe classe2 = classeRepository.save(newClasse(502));

        Student studentA = studentRepository.save(newStudent("Alpha", classe1));
        Student studentB = studentRepository.save(newStudent("Beta", classe2));
        studentAId = studentA.getId();
        studentBId = studentB.getId();

        Parent parentA = new Parent();
        parentA.setNom("Parent");
        parentA.setPrenom("A");
        parentA.setEmail("obs.parentA@test.local");
        parentA.setUser(saveUser("obs.parentA@test.local", Role.PARENT));
        parentA.setStudents(Collections.singletonList(studentA));
        parentRepository.save(parentA);

        Teacher teacher = new Teacher();
        teacher.setNom("Prof");
        teacher.setPrenom("Obs");
        teacher.setUser(saveUser("obs.teacher@test.local", Role.TEACHER));
        teacher.setClasses(new HashSet<>(Collections.singletonList(classe1)));
        teacherRepository.save(teacher);

        // A visible and a private observation for student A, plus a teacher.
        saveObservation(studentA, teacher, "Great participation", true);
        saveObservation(studentA, teacher, "Internal note: monitor closely", false);
    }

    @Test
    @WithMockUser(username = "obs.teacher@test.local", roles = "TEACHER")
    void teacherCanCreateObservationForOwnClassStudent() throws Exception {
        mockMvc.perform(post("/api/observations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(studentAId))))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "obs.teacher@test.local", roles = "TEACHER")
    void teacherCannotCreateObservationForStudentOutsideClasses() throws Exception {
        mockMvc.perform(post("/api/observations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(studentBId))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "obs.teacher@test.local", roles = "TEACHER")
    void teacherSeesAllObservationsIncludingPrivate() throws Exception {
        mockMvc.perform(get("/api/students/" + studentAId + "/observations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(username = "obs.parentA@test.local", roles = "PARENT")
    void parentSeesOnlyVisibleObservations() throws Exception {
        mockMvc.perform(get("/api/students/" + studentAId + "/observations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].visibleToParents").value(true));
    }

    @Test
    @WithMockUser(username = "obs.parentA@test.local", roles = "PARENT")
    void parentCannotSeeAnotherChildObservations() throws Exception {
        mockMvc.perform(get("/api/students/" + studentBId + "/observations"))
                .andExpect(status().isForbidden());
    }

    private ObservationRequest request(Long studentId) {
        ObservationRequest r = new ObservationRequest();
        r.setStudentId(studentId);
        r.setType(ObservationType.PARTICIPATION);
        r.setMessage("Actively participated today.");
        return r;
    }

    private void saveObservation(Student student, Teacher teacher, String message, boolean visible) {
        TeacherObservation o = new TeacherObservation();
        o.setStudent(student);
        o.setTeacher(teacher);
        o.setType(ObservationType.ACADEMIC);
        o.setMessage(message);
        o.setDate(LocalDate.now());
        o.setVisibleToParents(visible);
        observationRepository.save(o);
    }

    private Classe newClasse(int salle) {
        Classe c = new Classe();
        c.setSalle(salle);
        return c;
    }

    private Student newStudent(String nom, Classe classe) {
        Student s = new Student();
        s.setNom(nom);
        s.setPrenom("Test");
        s.setAge(14);
        s.setClasse(classe);
        return s;
    }

    private User saveUser(String email, Role role) {
        User u = new User();
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode("password123"));
        u.setRole(role);
        u.setEnabled(true);
        return userRepository.save(u);
    }
}
