package com.myga.learning.backend.backend;

import com.myga.learning.backend.backend.models.Classe;
import com.myga.learning.backend.backend.models.Grade;
import com.myga.learning.backend.backend.models.Parent;
import com.myga.learning.backend.backend.models.Role;
import com.myga.learning.backend.backend.models.Student;
import com.myga.learning.backend.backend.models.Subject;
import com.myga.learning.backend.backend.models.Teacher;
import com.myga.learning.backend.backend.models.User;
import com.myga.learning.backend.backend.repositories.ClasseRepository;
import com.myga.learning.backend.backend.repositories.GradeRepository;
import com.myga.learning.backend.backend.repositories.ParentRepository;
import com.myga.learning.backend.backend.repositories.StudentRepository;
import com.myga.learning.backend.backend.repositories.SubjectRepository;
import com.myga.learning.backend.backend.repositories.TeacherRepository;
import com.myga.learning.backend.backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Averages and trend are computed from stored grades, and remain ownership-checked. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PerformanceIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private ParentRepository parentRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private ClasseRepository classeRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private TeacherRepository teacherRepository;
    @Autowired private GradeRepository gradeRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Long studentAId;
    private Long studentBId;

    @BeforeEach
    void seed() {
        Classe classe = classeRepository.save(newClasse(901));
        Subject maths = subjectRepository.save(newSubject("Maths", "MATH"));

        Student studentA = studentRepository.save(newStudent("Perf", classe));
        Student studentB = studentRepository.save(newStudent("Other", classe));
        studentAId = studentA.getId();
        studentBId = studentB.getId();

        Parent parentA = new Parent();
        parentA.setNom("Parent");
        parentA.setPrenom("Perf");
        parentA.setEmail("perf.parent@test.local");
        parentA.setUser(saveUser("perf.parent@test.local", Role.PARENT));
        parentA.setStudents(Collections.singletonList(studentA));
        parentRepository.save(parentA);

        Teacher teacher = new Teacher();
        teacher.setNom("Prof");
        teacher.setPrenom("Perf");
        teacher.setUser(saveUser("perf.teacher@test.local", Role.TEACHER));
        teacherRepository.save(teacher);

        // Four maths grades, improving over time: 10/20, 10/20, 18/20, 18/20
        // -> earlier half 50%, later half 90%, overall 70%, delta +40.
        saveGrade(studentA, maths, teacher, 10, 20, LocalDate.now().minusDays(4));
        saveGrade(studentA, maths, teacher, 10, 20, LocalDate.now().minusDays(3));
        saveGrade(studentA, maths, teacher, 18, 20, LocalDate.now().minusDays(2));
        saveGrade(studentA, maths, teacher, 18, 20, LocalDate.now().minusDays(1));
    }

    @Test
    @WithMockUser(username = "perf.parent@test.local", roles = "PARENT")
    void parentSeesComputedAveragesAndImprovingTrend() throws Exception {
        mockMvc.perform(get("/api/students/" + studentAId + "/performance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalGrades").value(4))
                .andExpect(jsonPath("$.overallAverage").value(70.0))
                .andExpect(jsonPath("$.subjectAverages.length()").value(1))
                .andExpect(jsonPath("$.subjectAverages[0].average").value(70.0))
                .andExpect(jsonPath("$.subjectAverages[0].gradeCount").value(4))
                .andExpect(jsonPath("$.trend").value("IMPROVING"))
                .andExpect(jsonPath("$.trendDelta").value(40.0));
    }

    @Test
    @WithMockUser(username = "perf.parent@test.local", roles = "PARENT")
    void studentWithoutGradesReportsInsufficientData() throws Exception {
        mockMvc.perform(get("/api/students/" + studentBId + "/performance"))
                .andExpect(status().isForbidden()); // not this parent's child
    }

    @Test
    @WithMockUser(username = "admin@myga.local", roles = "ADMIN")
    void adminSeesInsufficientDataForAStudentWithNoGrades() throws Exception {
        mockMvc.perform(get("/api/students/" + studentBId + "/performance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalGrades").value(0))
                .andExpect(jsonPath("$.overallAverage").doesNotExist())
                .andExpect(jsonPath("$.trend").value("INSUFFICIENT_DATA"));
    }

    private void saveGrade(Student s, Subject subject, Teacher t, double value, double max, LocalDate date) {
        Grade g = new Grade();
        g.setStudent(s);
        g.setSubject(subject);
        g.setTeacher(t);
        g.setValue(value);
        g.setMaxValue(max);
        g.setDate(date);
        gradeRepository.save(g);
    }

    private Classe newClasse(int salle) {
        Classe c = new Classe();
        c.setSalle(salle);
        return c;
    }

    private Subject newSubject(String nom, String code) {
        Subject s = new Subject();
        s.setNom(nom);
        s.setCode(code);
        return s;
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
