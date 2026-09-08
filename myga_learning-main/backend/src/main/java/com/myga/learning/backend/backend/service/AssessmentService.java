package com.myga.learning.backend.backend.service;

import com.myga.learning.backend.backend.dto.AssessmentRequest;
import com.myga.learning.backend.backend.dto.AssessmentResponse;
import com.myga.learning.backend.backend.exception.ResourceNotFoundException;
import com.myga.learning.backend.backend.mapper.AssessmentMapper;
import com.myga.learning.backend.backend.models.Assessment;
import com.myga.learning.backend.backend.models.Classe;
import com.myga.learning.backend.backend.models.Student;
import com.myga.learning.backend.backend.models.Subject;
import com.myga.learning.backend.backend.models.Teacher;
import com.myga.learning.backend.backend.repositories.AssessmentRepository;
import com.myga.learning.backend.backend.repositories.ClasseRepository;
import com.myga.learning.backend.backend.repositories.StudentRepository;
import com.myga.learning.backend.backend.repositories.SubjectRepository;
import com.myga.learning.backend.backend.repositories.TeacherRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Assessments. A teacher may only create them for a class they are assigned to
 * and a subject they teach; reads of a student's assessments reuse the shared
 * student read-ownership rule.
 */
@Service
@Transactional
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final SubjectRepository subjectRepository;
    private final ClasseRepository classeRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final CurrentUserService currentUserService;
    private final AcademicPeriodService academicPeriodService;

    public AssessmentService(AssessmentRepository assessmentRepository,
                             SubjectRepository subjectRepository,
                             ClasseRepository classeRepository,
                             TeacherRepository teacherRepository,
                             StudentRepository studentRepository,
                             CurrentUserService currentUserService,
                             AcademicPeriodService academicPeriodService) {
        this.assessmentRepository = assessmentRepository;
        this.subjectRepository = subjectRepository;
        this.classeRepository = classeRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.currentUserService = currentUserService;
        this.academicPeriodService = academicPeriodService;
    }

    public AssessmentResponse create(AssessmentRequest request) {
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> ResourceNotFoundException.of("Subject", request.getSubjectId()));
        Classe classe = classeRepository.findById(request.getClasseId())
                .orElseThrow(() -> ResourceNotFoundException.of("Classe", request.getClasseId()));
        Teacher teacher = resolveTeacher(request.getTeacherId(), classe, subject);

        Assessment assessment = new Assessment();
        assessment.setTitle(request.getTitle());
        assessment.setDescription(request.getDescription());
        assessment.setSubject(subject);
        assessment.setClasse(classe);
        assessment.setTeacher(teacher);
        assessment.setType(request.getType());
        assessment.setDate(request.getDate() != null ? request.getDate() : LocalDate.now());
        assessment.setMaxGrade(request.getMaxGrade());
        if (request.getSemesterId() != null) {
            assessment.setSemester(academicPeriodService.getSemesterOrThrow(request.getSemesterId()));
        }
        return AssessmentMapper.toResponse(assessmentRepository.save(assessment));
    }

    @Transactional(readOnly = true)
    public AssessmentResponse findById(Long id) {
        Assessment assessment = getAssessmentOrThrow(id);
        ensureCanRead(assessment);
        return AssessmentMapper.toResponse(assessment);
    }

    /** Assessments for a student's class; ownership-checked against the caller. */
    @Transactional(readOnly = true)
    public List<AssessmentResponse> findForStudent(Long studentId, boolean upcomingOnly) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Student", studentId));
        currentUserService.ensureCanReadStudent(student);
        if (student.getClasse() == null) {
            return List.of();
        }
        Long classeId = student.getClasse().getId();
        List<Assessment> assessments = upcomingOnly
                ? assessmentRepository.findByClasse_IdAndDateGreaterThanEqualOrderByDateAsc(classeId, LocalDate.now())
                : assessmentRepository.findByClasse_IdOrderByDateDesc(classeId);
        return assessments.stream().map(AssessmentMapper::toResponse).collect(Collectors.toList());
    }

    /** The authenticated teacher's own assessments. */
    @Transactional(readOnly = true)
    public List<AssessmentResponse> findMine() {
        Teacher teacher = currentUserService.getCurrentTeacher();
        return assessmentRepository.findByTeacher_IdOrderByDateDesc(teacher.getId()).stream()
                .map(AssessmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    private void ensureCanRead(Assessment assessment) {
        if (currentUserService.hasRole("ADMIN")) {
            return;
        }
        if (currentUserService.hasRole("TEACHER")) {
            Teacher teacher = currentUserService.getCurrentTeacher();
            if (!currentUserService.teacherTeachesClasse(teacher, assessment.getClasse().getId())) {
                throw new AccessDeniedException("This assessment is not for one of your classes");
            }
            return;
        }
        if (currentUserService.hasRole("PARENT")) {
            boolean forOwnChild = currentUserService.getCurrentParent().getStudents() != null
                    && currentUserService.getCurrentParent().getStudents().stream()
                    .anyMatch(s -> s.getClasse() != null
                            && s.getClasse().getId().equals(assessment.getClasse().getId()));
            if (!forOwnChild) {
                throw new AccessDeniedException("This assessment does not concern your children");
            }
            return;
        }
        throw new AccessDeniedException("Not allowed to view this assessment");
    }

    private Teacher resolveTeacher(Long requestedTeacherId, Classe classe, Subject subject) {
        if (currentUserService.hasRole("TEACHER")) {
            Teacher teacher = currentUserService.getCurrentTeacher();
            if (!currentUserService.teacherTeachesClasse(teacher, classe.getId())) {
                throw new AccessDeniedException("This class is not one of your assigned classes");
            }
            boolean assignedToSubject = teacher.getSubjects().stream()
                    .anyMatch(s -> s.getId().equals(subject.getId()));
            if (!assignedToSubject) {
                throw new AccessDeniedException("You are not assigned to this subject");
            }
            return teacher;
        }
        if (requestedTeacherId == null) {
            throw new IllegalArgumentException("teacherId is required when an admin creates an assessment");
        }
        return teacherRepository.findById(requestedTeacherId)
                .orElseThrow(() -> ResourceNotFoundException.of("Teacher", requestedTeacherId));
    }

    private Assessment getAssessmentOrThrow(Long id) {
        return assessmentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Assessment", id));
    }
}
