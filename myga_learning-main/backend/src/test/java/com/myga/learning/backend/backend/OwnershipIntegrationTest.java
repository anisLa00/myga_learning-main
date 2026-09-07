package com.myga.learning.backend.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myga.learning.backend.backend.dto.GradeRequest;
import com.myga.learning.backend.backend.models.Classe;
import com.myga.learning.backend.backend.models.Parent;
import com.myga.learning.backend.backend.models.Role;
import com.myga.learning.backend.backend.models.Student;
import com.myga.learning.backend.backend.models.Subject;
import com.myga.learning.backend.backend.models.Teacher;
import com.myga.learning.backend.backend.models.User;
import com.myga.learning.backend.backend.repositories.ClasseRepository;
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

/**
 * Proves the P0.5 ownership rules are enforced server-side (IDOR protection):
 * a parent can only reach their own children, and a teacher can only grade
 * students in their assigned classes for their assigned subjects. The class is
 * @Transactional so each test rolls back its seed data.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OwnershipIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private ParentRepository parentRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private ClasseRepository classeRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private TeacherRepository teacherRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Long studentAId;
    private Long studentBId;
    private Long subjectMathId;
    private Long subjectHistoryId;

    @BeforeEach
    void seed() {
        Classe classe1 = classeRepository.save(newClasse(101));
        Classe classe2 = classeRepository.save(newClasse(102));

        Subject math = subjectRepository.save(newSubject("Mathematics", "MATH"));
        Subject history = subjectRepository.save(newSubject("History", "HIST"));
        subjectMathId = math.getId();
        subjectHistoryId = history.getId();

        Student studentA = studentRepository.save(newStudent("Alpha", classe1));
        Student studentB = studentRepository.save(newStudent("Beta", classe2));
        studentAId = studentA.getId();
        studentBId = studentB.getId();

        // Parent A owns student A; Parent B owns student B.
        Parent parentA = newParent("parentA@test.local");
        parentA.setStudents(Collections.singletonList(studentA));
        parentRepository.save(parentA);

        Parent parentB = newParent("parentB@test.local");
        parentB.setStudents(Collections.singletonList(studentB));
        parentRepository.save(parentB);

        // Teacher teaches Mathematics for classe1 only.
        User teacherUser = saveUser("teacher@test.local", Role.TEACHER);
        Teacher teacher = new Teacher();
        teacher.setNom("Prof");
        teacher.setPrenom("One");
        teacher.setUser(teacherUser);
        teacher.setSubjects(new HashSet<>(Collections.singletonList(math)));
        teacher.setClasses(new HashSet<>(Collections.singletonList(classe1)));
        teacherRepository.save(teacher);
    }

    // ----- Parent ownership -----

    @Test
    @WithMockUser(username = "parentA@test.local", roles = "PARENT")
    void parentCanReadOwnChildGrades() throws Exception {
        mockMvc.perform(get("/api/students/" + studentAId + "/grades"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "parentA@test.local", roles = "PARENT")
    void parentCannotReadOtherChildGrades() throws Exception {
        mockMvc.perform(get("/api/students/" + studentBId + "/grades"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "parentA@test.local", roles = "PARENT")
    void parentMeChildrenReturnsOnlyOwnChild() throws Exception {
        mockMvc.perform(get("/api/parent/me/children"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(studentAId));
    }

    // ----- Teacher ownership -----

    @Test
    @WithMockUser(username = "teacher@test.local", roles = "TEACHER")
    void teacherCanGradeAssignedSubjectForStudentInClass() throws Exception {
        mockMvc.perform(post("/api/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gradeRequest(studentAId, subjectMathId))))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "teacher@test.local", roles = "TEACHER")
    void teacherCannotGradeStudentOutsideTheirClasses() throws Exception {
        mockMvc.perform(post("/api/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gradeRequest(studentBId, subjectMathId))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "teacher@test.local", roles = "TEACHER")
    void teacherCannotGradeSubjectNotAssignedToThem() throws Exception {
        mockMvc.perform(post("/api/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gradeRequest(studentAId, subjectHistoryId))))
                .andExpect(status().isForbidden());
    }

    // ----- helpers -----

    private GradeRequest gradeRequest(Long studentId, Long subjectId) {
        GradeRequest request = new GradeRequest();
        request.setStudentId(studentId);
        request.setSubjectId(subjectId);
        request.setValue(15.0);
        request.setMaxValue(20.0);
        return request;
    }

    private Classe newClasse(int salle) {
        Classe classe = new Classe();
        classe.setSalle(salle);
        return classe;
    }

    private Subject newSubject(String nom, String code) {
        Subject subject = new Subject();
        subject.setNom(nom);
        subject.setCode(code);
        return subject;
    }

    private Student newStudent(String nom, Classe classe) {
        Student student = new Student();
        student.setNom(nom);
        student.setPrenom("Test");
        student.setAge(14);
        student.setClasse(classe);
        return student;
    }

    private Parent newParent(String email) {
        Parent parent = new Parent();
        parent.setNom("Parent");
        parent.setPrenom("Test");
        parent.setEmail(email);
        parent.setUser(saveUser(email, Role.PARENT));
        return parent;
    }

    private User saveUser(String email, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(role);
        user.setEnabled(true);
        return userRepository.save(user);
    }
}
