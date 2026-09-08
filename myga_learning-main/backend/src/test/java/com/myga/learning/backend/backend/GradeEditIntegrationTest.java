package com.myga.learning.backend.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myga.learning.backend.backend.dto.GradeRequest;
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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A teacher may edit and delete only the grades they recorded themselves;
 * admins may modify any. Also covers the class attendance register.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GradeEditIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private ClasseRepository classeRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private TeacherRepository teacherRepository;
    @Autowired private GradeRepository gradeRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Long classeId;
    private Long subjectId;
    private Long studentId;
    private Long gradeOfTeacherAId;

    @BeforeEach
    void seed() {
        Classe classe = classeRepository.save(newClasse(1001));
        Subject maths = subjectRepository.save(newSubject("Maths", "MATH"));
        Student student = studentRepository.save(newStudent(classe));
        classeId = classe.getId();
        subjectId = maths.getId();
        studentId = student.getId();

        Teacher teacherA = newTeacher("edit.teacherA@test.local", classe, maths);
        newTeacher("edit.teacherB@test.local", classe, maths);

        Grade grade = new Grade();
        grade.setStudent(student);
        grade.setSubject(maths);
        grade.setTeacher(teacherA);
        grade.setValue(12);
        grade.setMaxValue(20);
        grade.setDate(LocalDate.now());
        gradeOfTeacherAId = gradeRepository.save(grade).getId();
    }

    @Test
    @WithMockUser(username = "edit.teacherA@test.local", roles = "TEACHER")
    void teacherCanEditTheirOwnGrade() throws Exception {
        mockMvc.perform(put("/api/grades/" + gradeOfTeacherAId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(18.0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value(18.0));
    }

    @Test
    @WithMockUser(username = "edit.teacherB@test.local", roles = "TEACHER")
    void teacherCannotEditAnotherTeachersGrade() throws Exception {
        mockMvc.perform(put("/api/grades/" + gradeOfTeacherAId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(20.0))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "edit.teacherB@test.local", roles = "TEACHER")
    void teacherCannotDeleteAnotherTeachersGrade() throws Exception {
        mockMvc.perform(delete("/api/grades/" + gradeOfTeacherAId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "edit.teacherA@test.local", roles = "TEACHER")
    void teacherCanDeleteTheirOwnGrade() throws Exception {
        mockMvc.perform(delete("/api/grades/" + gradeOfTeacherAId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin@myga.local", roles = "ADMIN")
    void adminCanEditAnyGrade() throws Exception {
        mockMvc.perform(put("/api/grades/" + gradeOfTeacherAId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(5.0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value(5.0));
    }

    @Test
    @WithMockUser(username = "edit.teacherA@test.local", roles = "TEACHER")
    void teacherCanReadTheRegisterOfTheirOwnClass() throws Exception {
        mockMvc.perform(get("/api/classes/" + classeId + "/attendance"))
                .andExpect(status().isOk());
    }

    private GradeRequest request(double value) {
        GradeRequest r = new GradeRequest();
        r.setStudentId(studentId);
        r.setSubjectId(subjectId);
        r.setValue(value);
        r.setMaxValue(20.0);
        return r;
    }

    private Teacher newTeacher(String email, Classe classe, Subject subject) {
        Teacher t = new Teacher();
        t.setNom("Prof");
        t.setPrenom(email);
        t.setUser(saveUser(email, Role.TEACHER));
        t.setClasses(new HashSet<>(Collections.singletonList(classe)));
        t.setSubjects(new HashSet<>(Collections.singletonList(subject)));
        return teacherRepository.save(t);
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

    private Student newStudent(Classe classe) {
        Student s = new Student();
        s.setNom("Edit");
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
