package com.myga.learning.backend.backend.service;

import com.myga.learning.backend.backend.dto.ObservationRequest;
import com.myga.learning.backend.backend.dto.ObservationResponse;
import com.myga.learning.backend.backend.exception.ResourceNotFoundException;
import com.myga.learning.backend.backend.mapper.ObservationMapper;
import com.myga.learning.backend.backend.models.NotificationType;
import com.myga.learning.backend.backend.models.Student;
import com.myga.learning.backend.backend.models.Subject;
import com.myga.learning.backend.backend.models.Teacher;
import com.myga.learning.backend.backend.models.TeacherObservation;
import com.myga.learning.backend.backend.repositories.ObservationRepository;
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
 * Teacher observations. Creation is restricted to a teacher of the student's
 * class (or an admin). On read, parents see only observations flagged visible;
 * staff (admins, teachers of the class) see everything, including private
 * internal notes.
 */
@Service
@Transactional
public class ObservationService {

    private final ObservationRepository observationRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;

    public ObservationService(ObservationRepository observationRepository,
                              StudentRepository studentRepository,
                              SubjectRepository subjectRepository,
                              TeacherRepository teacherRepository,
                              CurrentUserService currentUserService,
                              NotificationService notificationService) {
        this.observationRepository = observationRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.teacherRepository = teacherRepository;
        this.currentUserService = currentUserService;
        this.notificationService = notificationService;
    }

    public ObservationResponse create(ObservationRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> ResourceNotFoundException.of("Student", request.getStudentId()));
        Subject subject = resolveSubject(request.getSubjectId());
        Teacher teacher = resolveTeacher(request.getTeacherId(), student);
        boolean visible = request.getVisibleToParents() == null || request.getVisibleToParents();

        TeacherObservation observation = new TeacherObservation();
        observation.setStudent(student);
        observation.setSubject(subject);
        observation.setTeacher(teacher);
        observation.setType(request.getType());
        observation.setMessage(request.getMessage());
        observation.setDate(request.getDate() != null ? request.getDate() : LocalDate.now());
        observation.setVisibleToParents(visible);

        TeacherObservation saved = observationRepository.save(observation);

        if (visible) {
            notificationService.notifyStudentParents(student, NotificationType.NEW_OBSERVATION,
                    "New feedback for " + student.getPrenom() + " " + student.getNom(),
                    "A teacher recorded new feedback (" + request.getType() + ").");
        }
        return ObservationMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ObservationResponse> getStudentObservations(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Student", studentId));
        currentUserService.ensureCanReadStudent(student);

        boolean staff = currentUserService.hasRole("ADMIN") || currentUserService.hasRole("TEACHER");
        return observationRepository.findByStudent_IdOrderByDateDesc(studentId).stream()
                .filter(o -> staff || o.isVisibleToParents())
                .map(ObservationMapper::toResponse)
                .collect(Collectors.toList());
    }

    private Subject resolveSubject(Long subjectId) {
        if (subjectId == null) {
            return null;
        }
        return subjectRepository.findById(subjectId)
                .orElseThrow(() -> ResourceNotFoundException.of("Subject", subjectId));
    }

    private Teacher resolveTeacher(Long requestedTeacherId, Student student) {
        if (currentUserService.hasRole("TEACHER")) {
            Teacher teacher = currentUserService.getCurrentTeacher();
            if (!currentUserService.teacherTeachesStudent(teacher, student)) {
                throw new AccessDeniedException("This student is not in one of your classes");
            }
            return teacher;
        }
        if (requestedTeacherId == null) {
            throw new IllegalArgumentException("teacherId is required when an admin records an observation");
        }
        return teacherRepository.findById(requestedTeacherId)
                .orElseThrow(() -> ResourceNotFoundException.of("Teacher", requestedTeacherId));
    }
}
