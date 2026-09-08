package com.myga.learning.backend.backend;

import com.myga.learning.backend.backend.models.Classe;
import com.myga.learning.backend.backend.models.Grade;
import com.myga.learning.backend.backend.models.Role;
import com.myga.learning.backend.backend.models.Student;
import com.myga.learning.backend.backend.models.Subject;
import com.myga.learning.backend.backend.models.Teacher;
import com.myga.learning.backend.backend.models.User;
import com.myga.learning.backend.backend.repositories.ClasseRepository;
import com.myga.learning.backend.backend.repositories.GradeRepository;
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
import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Paged student listing with filters, and the scoped grade search. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PaginationFilterIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private ClasseRepository classeRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private TeacherRepository teacherRepository;
    @Autowired private GradeRepository gradeRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Long taughtClasseId;

    @BeforeEach
    void seed() {
        Classe taught = classeRepository.save(newClasse(1101));
        Classe other = classeRepository.save(newClasse(1102));
        taughtClasseId = taught.getId();

        Subject maths = subjectRepository.save(newSubject("Maths"));

        Student inClass = studentRepository.save(newStudent("Pagination", taught));
        Student elsewhere = studentRepository.save(newStudent("Elsewhere", other));

        Teacher teacher = new Teacher();
        teacher.setNom("Prof");
        teacher.setPrenom("Page");
        teacher.setUser(saveUser("page.teacher@test.local", Role.TEACHER));
        teacher.setClasses(new HashSet<>(Collections.singletonList(taught)));
        teacher.setSubjects(new HashSet<>(Collections.singletonList(maths)));
        teacherRepository.save(teacher);

        // One grade in the teacher's class, one in a class they do not teach.
        saveGrade(inClass, maths, teacher);
        saveGrade(elsewhere, maths, teacher);
    }

    @Test
    @WithMockUser(username = "admin@myga.local", roles = "ADMIN")
    void studentListIsPagedAndExposesMetadata() throws Exception {
        mockMvc.perform(get("/api/students?page=0&size=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").exists());
    }

    @Test
    @WithMockUser(username = "admin@myga.local", roles = "ADMIN")
    void studentListCanBeFilteredByNameSearch() throws Exception {
        mockMvc.perform(get("/api/students?search=paginat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nom").value("Pagination"));
    }

    @Test
    @WithMockUser(username = "admin@myga.local", roles = "ADMIN")
    void studentListCanBeFilteredByClass() throws Exception {
        mockMvc.perform(get("/api/students?classeId=" + taughtClasseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nom").value("Pagination"));
    }

    @Test
    @WithMockUser(username = "page.teacher@test.local", roles = "TEACHER")
    void gradeSearchIsScopedToTheTeachersOwnClasses() throws Exception {
        // Two grades exist, but only the one in the teacher's class is visible.
        mockMvc.perform(get("/api/grades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].student.nom").value("Pagination"));
    }

    @Test
    @WithMockUser(username = "admin@myga.local", roles = "ADMIN")
    void gradeSearchCanBeFilteredByClass() throws Exception {
        mockMvc.perform(get("/api/grades?classeId=" + taughtClasseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    private void saveGrade(Student s, Subject subject, Teacher t) {
        Grade g = new Grade();
        g.setStudent(s);
        g.setSubject(subject);
        g.setTeacher(t);
        g.setValue(10);
        g.setMaxValue(20);
        g.setDate(LocalDate.now());
        gradeRepository.save(g);
    }

    private Classe newClasse(int salle) {
        Classe c = new Classe();
        c.setSalle(salle);
        return c;
    }

    private Subject newSubject(String nom) {
        Subject s = new Subject();
        s.setNom(nom);
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
