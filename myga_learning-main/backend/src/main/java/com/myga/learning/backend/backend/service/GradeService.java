package com.myga.learning.backend.backend.service;

import com.myga.learning.backend.backend.dto.GradeRequest;
import com.myga.learning.backend.backend.dto.GradeResponse;
import com.myga.learning.backend.backend.exception.ResourceNotFoundException;
import com.myga.learning.backend.backend.mapper.GradeMapper;
import com.myga.learning.backend.backend.models.Grade;
import com.myga.learning.backend.backend.models.NotificationType;
import com.myga.learning.backend.backend.models.Student;
import com.myga.learning.backend.backend.models.Subject;
import com.myga.learning.backend.backend.models.Teacher;
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
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;

    public GradeService(GradeRepository gradeRepository,
                        StudentRepository studentRepository,
                        SubjectRepository subjectRepository,
                        TeacherRepository teacherRepository,
                        CurrentUserService currentUserService,
                        NotificationService notificationService) {
        this.gradeRepository = gradeRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.teacherRepository = teacherRepository;
        this.currentUserService = currentUserService;
        this.notificationService = notificationService;
    }

    public GradeResponse create(GradeRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> ResourceNotFoundException.of("Student", request.getStudentId()));
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> ResourceNotFoundException.of("Subject", request.getSubjectId()));

        Teacher teacher = resolveTeacherForCreate(request, student, subject);

        Grade grade = new Grade();
        grade.setStudent(student);
        grade.setSubject(subject);
        grade.setTeacher(teacher);
        grade.setValue(request.getValue());
        grade.setMaxValue(request.getMaxValue());
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

    private Teacher resolveTeacherForCreate(GradeRequest request, Student student, Subject subject) {
        if (currentUserService.hasRole("TEACHER")) {
            Teacher teacher = currentUserService.getCurrentTeacher();
            boolean assignedToSubject = teacher.getSubjects().stream()
                    .anyMatch(s -> s.getId().equals(subject.getId()));
            if (!assignedToSubject) {
                throw new AccessDeniedException("You are not assigned to this subject");
            }
            if (!currentUserService.teacherTeachesStudent(teacher, student)) {
                throw new AccessDeniedException("This student is not in one of your classes");
            }
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
