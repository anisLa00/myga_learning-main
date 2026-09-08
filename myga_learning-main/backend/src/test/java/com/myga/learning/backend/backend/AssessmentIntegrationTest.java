package com.myga.learning.backend.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myga.learning.backend.backend.dto.AssessmentRequest;
import com.myga.learning.backend.backend.dto.GradeRequest;
import com.myga.learning.backend.backend.models.AcademicYear;
import com.myga.learning.backend.backend.models.Assessment;
import com.myga.learning.backend.backend.models.AssessmentType;
import com.myga.learning.backend.backend.models.Classe;
import com.myga.learning.backend.backend.models.Parent;
import com.myga.learning.backend.backend.models.Role;
import com.myga.learning.backend.backend.models.Semester;
import com.myga.learning.backend.backend.models.Student;
import com.myga.learning.backend.backend.models.Subject;
import com.myga.learning.backend.backend.models.Teacher;
import com.myga.learning.backend.backend.models.User;
import com.myga.learning.backend.backend.repositories.AcademicYearRepository;
import com.myga.learning.backend.backend.repositories.AssessmentRepository;
import com.myga.learning.backend.backend.repositories.ClasseRepository;
import com.myga.learning.backend.backend.repositories.ParentRepository;
import com.myga.learning.backend.backend.repositories.SemesterRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Assessment ownership, and the rule that a grade attached to an assessment
 * inherits that assessment's subject, maximum grade and semester.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AssessmentIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private ParentRepository parentRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private ClasseRepository classeRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private TeacherRepository teacherRepository;
    @Autowired private AssessmentRepository assessmentRepository;
    @Autowired private AcademicYearRepository academicYearRepository;
    @Autowired private SemesterRepository semesterRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Long classe1Id;
    private Long classe2Id;
    private Long mathId;
    private Long historyId;
    private Long studentAId;
    private Long assessmentId;
    private Long semesterId;

    @BeforeEach
    void seed() {
        AcademicYear year = new AcademicYear();
        year.setLabel("2025-2026-assess");
        year.setCurrent(true);
        academicYearRepository.save(year);

        Semester semester = new Semester();
        semester.setLabel("Semester 1");
        semester.setAcademicYear(year);
        semesterId = semesterRepository.save(semester).getId();

        Classe classe1 = classeRepository.save(newClasse(801));
        Classe classe2 = classeRepository.save(newClasse(802));
        classe1Id = classe1.getId();
        classe2Id = classe2.getId();

        Subject math = subjectRepository.save(newSubject("Maths", "MATH"));
        Subject history = subjectRepository.save(newSubject("History", "HIST"));
        mathId = math.getId();
        historyId = history.getId();

        Student studentA = studentRepository.save(newStudent("Alpha", classe1));
        studentAId = studentA.getId();

        Parent parentA = new Parent();
        parentA.setNom("Parent");
        parentA.setPrenom("A");
        parentA.setEmail("assess.parent@test.local");
        parentA.setUser(saveUser("assess.parent@test.local", Role.PARENT));
        parentA.setStudents(Collections.singletonList(studentA));
        parentRepository.save(parentA);

        Teacher teacher = new Teacher();
        teacher.setNom("Prof");
        teacher.setPrenom("Assess");
        teacher.setUser(saveUser("assess.teacher@test.local", Role.TEACHER));
        teacher.setClasses(new HashSet<>(Collections.singletonList(classe1)));
        teacher.setSubjects(new HashSet<>(Collections.singletonList(math)));
        teacherRepository.save(teacher);

        // A future assessment for classe1 / maths, worth 50 points.
        Assessment assessment = new Assessment();
        assessment.setTitle("Algebra exam");
        assessment.setSubject(math);
        assessment.setClasse(classe1);
        assessment.setTeacher(teacher);
        assessment.setType(AssessmentType.EXAM);
        assessment.setDate(LocalDate.now().plusDays(7));
        assessment.setMaxGrade(50);
        assessment.setSemester(semester);
        assessmentId = assessmentRepository.save(assessment).getId();
    }

    @Test
    @WithMockUser(username = "assess.teacher@test.local", roles = "TEACHER")
    void teacherCanCreateAssessmentForOwnClassAndSubject() throws Exception {
        mockMvc.perform(post("/api/assessments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(classe1Id, mathId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("QUIZ"))
                .andExpect(jsonPath("$.academicYear").value("2025-2026-assess"));
    }

    @Test
    @WithMockUser(username = "assess.teacher@test.local", roles = "TEACHER")
    void teacherCannotCreateAssessmentForAnotherClass() throws Exception {
        mockMvc.perform(post("/api/assessments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(classe2Id, mathId))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "assess.teacher@test.local", roles = "TEACHER")
    void teacherCannotCreateAssessmentForUnassignedSubject() throws Exception {
        mockMvc.perform(post("/api/assessments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(classe1Id, historyId))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "assess.teacher@test.local", roles = "TEACHER")
    void gradeLinkedToAssessmentInheritsSubjectMaxGradeAndSemester() throws Exception {
        GradeRequest grade = new GradeRequest();
        grade.setStudentId(studentAId);
        grade.setAssessmentId(assessmentId);   // no subjectId / maxValue supplied
        grade.setValue(42.0);

        mockMvc.perform(post("/api/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(grade)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subject.id").value(mathId))
                .andExpect(jsonPath("$.maxValue").value(50.0))
                .andExpect(jsonPath("$.assessmentId").value(assessmentId))
                .andExpect(jsonPath("$.semesterId").value(semesterId))
                .andExpect(jsonPath("$.academicYear").value("2025-2026-assess"));
    }

    @Test
    @WithMockUser(username = "assess.parent@test.local", roles = "PARENT")
    void parentSeesUpcomingAssessmentsForTheirChild() throws Exception {
        mockMvc.perform(get("/api/students/" + studentAId + "/assessments?upcoming=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Algebra exam"));
    }

    private AssessmentRequest request(Long classeId, Long subjectId) {
        AssessmentRequest r = new AssessmentRequest();
        r.setTitle("Weekly quiz");
        r.setClasseId(classeId);
        r.setSubjectId(subjectId);
        r.setType(AssessmentType.QUIZ);
        r.setMaxGrade(20.0);
        r.setSemesterId(semesterId);
        return r;
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
