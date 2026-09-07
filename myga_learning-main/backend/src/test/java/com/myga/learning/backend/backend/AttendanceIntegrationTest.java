package com.myga.learning.backend.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myga.learning.backend.backend.dto.AttendanceRequest;
import com.myga.learning.backend.backend.models.AttendanceStatus;
import com.myga.learning.backend.backend.models.Classe;
import com.myga.learning.backend.backend.models.Parent;
import com.myga.learning.backend.backend.models.Role;
import com.myga.learning.backend.backend.models.Student;
import com.myga.learning.backend.backend.models.Teacher;
import com.myga.learning.backend.backend.models.User;
import com.myga.learning.backend.backend.repositories.ClasseRepository;
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

import java.util.Collections;
import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Ownership rules for attendance mirror those for grades. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AttendanceIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private ParentRepository parentRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private ClasseRepository classeRepository;
    @Autowired private TeacherRepository teacherRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Long classe1Id;
    private Long studentAId;
    private Long studentBId;

    @BeforeEach
    void seed() {
        Classe classe1 = classeRepository.save(newClasse(201));
        Classe classe2 = classeRepository.save(newClasse(202));
        classe1Id = classe1.getId();

        Student studentA = studentRepository.save(newStudent("Alpha", classe1));
        Student studentB = studentRepository.save(newStudent("Beta", classe2));
        studentAId = studentA.getId();
        studentBId = studentB.getId();

        Parent parentA = new Parent();
        parentA.setNom("Parent");
        parentA.setPrenom("A");
        parentA.setEmail("attend.parentA@test.local");
        parentA.setUser(saveUser("attend.parentA@test.local", Role.PARENT));
        parentA.setStudents(Collections.singletonList(studentA));
        parentRepository.save(parentA);

        // Teacher teaches classe1 only.
        Teacher teacher = new Teacher();
        teacher.setNom("Prof");
        teacher.setPrenom("Att");
        teacher.setUser(saveUser("attend.teacher@test.local", Role.TEACHER));
        teacher.setClasses(new HashSet<>(Collections.singletonList(classe1)));
        teacherRepository.save(teacher);
    }

    @Test
    @WithMockUser(username = "attend.teacher@test.local", roles = "TEACHER")
    void teacherCanMarkStudentInOwnClass() throws Exception {
        mockMvc.perform(post("/api/attendance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(studentAId, classe1Id))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PRESENT"));
    }

    @Test
    @WithMockUser(username = "attend.teacher@test.local", roles = "TEACHER")
    void teacherCannotMarkStudentInAnotherClass() throws Exception {
        // studentB is in classe2, which this teacher does not teach.
        mockMvc.perform(post("/api/attendance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(studentBId, classe1Id))))
                // classe1 is theirs, but studentB isn't in it -> 400
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "attend.parentA@test.local", roles = "PARENT")
    void parentCanReadOwnChildAttendanceSummary() throws Exception {
        mockMvc.perform(get("/api/students/" + studentAId + "/attendance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").exists())
                .andExpect(jsonPath("$.attendancePercentage").exists());
    }

    @Test
    @WithMockUser(username = "attend.parentA@test.local", roles = "PARENT")
    void parentCannotReadOtherChildAttendance() throws Exception {
        mockMvc.perform(get("/api/students/" + studentBId + "/attendance"))
                .andExpect(status().isForbidden());
    }

    private AttendanceRequest request(Long studentId, Long classeId) {
        AttendanceRequest r = new AttendanceRequest();
        r.setStudentId(studentId);
        r.setClasseId(classeId);
        r.setStatus(AttendanceStatus.PRESENT);
        return r;
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
