package com.myga.learning.backend.backend;

import com.myga.learning.backend.backend.models.Attendance;
import com.myga.learning.backend.backend.models.AttendanceStatus;
import com.myga.learning.backend.backend.models.Classe;
import com.myga.learning.backend.backend.models.Parent;
import com.myga.learning.backend.backend.models.Role;
import com.myga.learning.backend.backend.models.Student;
import com.myga.learning.backend.backend.models.Teacher;
import com.myga.learning.backend.backend.models.User;
import com.myga.learning.backend.backend.repositories.AttendanceRepository;
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

/**
 * Dashboard aggregates are scoped to the caller and each endpoint is limited to
 * its own role.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DashboardIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private ParentRepository parentRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private ClasseRepository classeRepository;
    @Autowired private TeacherRepository teacherRepository;
    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void seed() {
        Classe classe = classeRepository.save(newClasse(601));
        Student child = studentRepository.save(newStudent("Dash", classe));

        Parent parent = new Parent();
        parent.setNom("Parent");
        parent.setPrenom("Dash");
        parent.setEmail("dash.parent@test.local");
        parent.setUser(saveUser("dash.parent@test.local", Role.PARENT));
        parent.setStudents(Collections.singletonList(child));
        parentRepository.save(parent);

        Teacher teacher = new Teacher();
        teacher.setNom("Prof");
        teacher.setPrenom("Dash");
        teacher.setUser(saveUser("dash.teacher@test.local", Role.TEACHER));
        teacher.setClasses(new HashSet<>(Collections.singletonList(classe)));
        teacherRepository.save(teacher);

        // One PRESENT and one ABSENT record -> 50% attendance for the child.
        attendanceRepository.save(record(child, classe, teacher, AttendanceStatus.PRESENT));
        attendanceRepository.save(record(child, classe, teacher, AttendanceStatus.ABSENT));
    }

    @Test
    @WithMockUser(username = "admin@myga.local", roles = "ADMIN")
    void adminDashboardReturnsTotals() throws Exception {
        mockMvc.perform(get("/api/dashboard/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStudents").exists())
                .andExpect(jsonPath("$.totalTeachers").exists())
                .andExpect(jsonPath("$.totalParents").exists())
                .andExpect(jsonPath("$.totalClasses").exists())
                .andExpect(jsonPath("$.recentAbsences").isArray());
    }

    @Test
    @WithMockUser(username = "dash.teacher@test.local", roles = "TEACHER")
    void teacherDashboardIsScopedToOwnAssignments() throws Exception {
        mockMvc.perform(get("/api/dashboard/teacher"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.classes.length()").value(1))
                .andExpect(jsonPath("$.totalStudents").value(1))
                .andExpect(jsonPath("$.unreadNotifications").exists());
    }

    @Test
    @WithMockUser(username = "dash.parent@test.local", roles = "PARENT")
    void parentDashboardShowsOwnChildrenAndAttendance() throws Exception {
        mockMvc.perform(get("/api/dashboard/parent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.children.length()").value(1))
                .andExpect(jsonPath("$.attendance.length()").value(1))
                .andExpect(jsonPath("$.attendance[0].total").value(2))
                .andExpect(jsonPath("$.attendance[0].totalAbsent").value(1))
                .andExpect(jsonPath("$.attendance[0].attendancePercentage").value(50.0))
                .andExpect(jsonPath("$.recentAbsences.length()").value(1));
    }

    @Test
    @WithMockUser(username = "dash.parent@test.local", roles = "PARENT")
    void parentCannotOpenTheAdminDashboard() throws Exception {
        mockMvc.perform(get("/api/dashboard/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "dash.teacher@test.local", roles = "TEACHER")
    void teacherCannotOpenTheParentDashboard() throws Exception {
        mockMvc.perform(get("/api/dashboard/parent"))
                .andExpect(status().isForbidden());
    }

    private Attendance record(Student student, Classe classe, Teacher teacher, AttendanceStatus status) {
        Attendance a = new Attendance();
        a.setStudent(student);
        a.setClasse(classe);
        a.setTeacher(teacher);
        a.setDate(LocalDate.now());
        a.setStatus(status);
        return a;
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
