package com.myga.learning.backend.backend.service;

import com.myga.learning.backend.backend.dto.GradeRequest;
import com.myga.learning.backend.backend.dto.GradeResponse;
import com.myga.learning.backend.backend.exception.ResourceNotFoundException;
import com.myga.learning.backend.backend.mapper.GradeMapper;
import com.myga.learning.backend.backend.models.Assessment;
import com.myga.learning.backend.backend.models.Grade;
import com.myga.learning.backend.backend.models.NotificationType;
import com.myga.learning.backend.backend.models.Semester;
import com.myga.learning.backend.backend.models.Student;
import com.myga.learning.backend.backend.models.Subject;
import com.myga.learning.backend.backend.models.Teacher;
import com.myga.learning.backend.backend.repositories.AssessmentRepository;
import com.myga.learning.backend.backend.repositories.GradeRepository;
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
 * Grade recording and retrieval with server-side ownership enforcement.
 *
 * <p>The client-supplied {@code studentId} is never trusted on its own: every
 * access is checked against the caller's own relationships (a parent's linked
 * children, a teacher's assigned classes and subjects). This is the primary
 * defence against IDOR.
 */
@Service
@Transactional
public class GradeService {

    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final AssessmentRepository assessmentRepository;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;
    private final AcademicPeriodService academicPeriodService;

    public GradeService(GradeRepository gradeRepository,
                        StudentRepository studentRepository,
                        SubjectRepository subjectRepository,
                        TeacherRepository teacherRepository,
                        AssessmentRepository assessmentRepository,
                        CurrentUserService currentUserService,
                        NotificationService notificationService,
                        AcademicPeriodService academicPeriodService) {
        this.gradeRepository = gradeRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.teacherRepository = teacherRepository;
        this.assessmentRepository = assessmentRepository;
        this.currentUserService = currentUserService;
        this.notificationService = notificationService;
        this.academicPeriodService = academicPeriodService;
    }

    public GradeResponse create(GradeRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> ResourceNotFoundException.of("Student", request.getStudentId()));

        ResolvedGrade resolved = resolve(request);
        Subject subject = resolved.subject;
        Teacher teacher = resolveTeacherForCreate(request, student, subject);

        Grade grade = new Grade();
        grade.setStudent(student);
        grade.setSubject(subject);
        grade.setTeacher(teacher);
        grade.setAssessment(resolved.assessment);
        grade.setSemester(resolved.semester);
        grade.setValue(request.getValue());
        grade.setMaxValue(resolved.maxValue);
        grade.setComment(request.getComment());
        grade.setDate(request.getDate() != null ? request.getDate() : LocalDate.now());
        Grade saved = gradeRepository.save(grade);

        notificationService.notifyStudentParents(student, NotificationType.NEW_GRADE,
                "New grade in " + subject.getNom(),
                student.getPrenom() + " " + student.getNom() + " received "
                        + saved.getValue() + "/" + saved.getMaxValue() + " in " + subject.getNom() + ".");

        return GradeMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<GradeResponse> getStudentGrades(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Student", studentId));
        currentUserService.ensureCanReadStudent(student);
        return gradeRepository.findByStudent_Id(studentId).stream()
                .map(GradeMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Updates a grade. A teacher may only change grades they recorded
     * themselves; an admin may change any.
     */
    public GradeResponse update(Long id, GradeRequest request) {
        Grade grade = getGradeOrThrow(id);
        ensureCanModify(grade);

        ResolvedGrade resolved = resolve(request);
        // A teacher must still be entitled to the (possibly changed) subject
        // and to the student's class.
        if (currentUserService.hasRole("TEACHER")) {
            assertTeacherMayGrade(currentUserService.getCurrentTeacher(), grade.getStudent(), resolved.subject);
        }

        grade.setSubject(resolved.subject);
        grade.setAssessment(resolved.assessment);
        grade.setSemester(resolved.semester);
        grade.setValue(request.getValue());
        grade.setMaxValue(resolved.maxValue);
        grade.setComment(request.getComment());
        if (request.getDate() != null) {
            grade.setDate(request.getDate());
        }
        return GradeMapper.toResponse(gradeRepository.save(grade));
    }

    /** Deletes a grade, under the same "own grades only" rule as update. */
    public void delete(Long id) {
        Grade grade = getGradeOrThrow(id);
        ensureCanModify(grade);
        gradeRepository.delete(grade);
    }

    private Grade getGradeOrThrow(Long id) {
        return gradeRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Grade", id));
    }

    /** Admins may modify any grade; a teacher only the ones they recorded. */
    private void ensureCanModify(Grade grade) {
        if (currentUserService.hasRole("ADMIN")) {
            return;
        }
        if (currentUserService.hasRole("TEACHER")) {
            Teacher teacher = currentUserService.getCurrentTeacher();
            if (grade.getTeacher() == null || !grade.getTeacher().getId().equals(teacher.getId())) {
                throw new AccessDeniedException("You can only modify grades you recorded yourself");
            }
            return;
        }
        throw new AccessDeniedException("Not allowed to modify grades");
    }

    /**
     * Resolves the subject, maximum grade and semester for a request. An
     * assessment, when given, is the source of truth so they can never
     * disagree with it; an explicit semester still wins.
     */
    private ResolvedGrade resolve(GradeRequest request) {
        ResolvedGrade resolved = new ResolvedGrade();
        if (request.getAssessmentId() != null) {
            resolved.assessment = assessmentRepository.findById(request.getAssessmentId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Assessment", request.getAssessmentId()));
            resolved.subject = resolved.assessment.getSubject();
            resolved.maxValue = resolved.assessment.getMaxGrade();
            resolved.semester = resolved.assessment.getSemester();
        } else {
            if (request.getSubjectId() == null) {
                throw new IllegalArgumentException("subjectId is required when no assessmentId is provided");
            }
            if (request.getMaxValue() == null) {
                throw new IllegalArgumentException("maxValue is required when no assessmentId is provided");
            }
            resolved.subject = subjectRepository.findById(request.getSubjectId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Subject", request.getSubjectId()));
            resolved.maxValue = request.getMaxValue();
        }
        if (request.getSemesterId() != null) {
            resolved.semester = academicPeriodService.getSemesterOrThrow(request.getSemesterId());
        }
        return resolved;
    }

    /** Holder for the values a grade derives from its request. */
    private static final class ResolvedGrade {
        private Assessment assessment;
        private Subject subject;
        private double maxValue;
        private Semester semester;
    }

    /** A teacher may only grade their own subject, for a student they teach. */
    private void assertTeacherMayGrade(Teacher teacher, Student student, Subject subject) {
        boolean assignedToSubject = teacher.getSubjects().stream()
                .anyMatch(s -> s.getId().equals(subject.getId()));
        if (!assignedToSubject) {
            throw new AccessDeniedException("You are not assigned to this subject");
        }
        if (!currentUserService.teacherTeachesStudent(teacher, student)) {
            throw new AccessDeniedException("This student is not in one of your classes");
        }
    }

    private Teacher resolveTeacherForCreate(GradeRequest request, Student student, Subject subject) {
        if (currentUserService.hasRole("TEACHER")) {
            Teacher teacher = currentUserService.getCurrentTeacher();
            assertTeacherMayGrade(teacher, student, subject);
            return teacher;
        }
        // ADMIN path: the teacher must be provided explicitly.
        if (request.getTeacherId() == null) {
            throw new IllegalArgumentException("teacherId is required when an admin records a grade");
        }
        return teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> ResourceNotFoundException.of("Teacher", request.getTeacherId()));
    }
}
