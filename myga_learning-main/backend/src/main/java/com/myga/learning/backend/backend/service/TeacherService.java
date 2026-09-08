package com.myga.learning.backend.backend.service;

import com.myga.learning.backend.backend.dto.ClasseSummaryResponse;
import com.myga.learning.backend.backend.dto.SubjectResponse;
import com.myga.learning.backend.backend.dto.TeacherRequest;
import com.myga.learning.backend.backend.dto.TeacherResponse;
import com.myga.learning.backend.backend.exception.ResourceNotFoundException;
import com.myga.learning.backend.backend.mapper.ClasseMapper;
import com.myga.learning.backend.backend.mapper.SubjectMapper;
import com.myga.learning.backend.backend.mapper.TeacherMapper;
import com.myga.learning.backend.backend.models.Classe;
import com.myga.learning.backend.backend.models.Role;
import com.myga.learning.backend.backend.models.Subject;
import com.myga.learning.backend.backend.models.Teacher;
import com.myga.learning.backend.backend.models.User;
import com.myga.learning.backend.backend.repositories.ClasseRepository;
import com.myga.learning.backend.backend.repositories.SubjectRepository;
import com.myga.learning.backend.backend.repositories.TeacherRepository;
import com.myga.learning.backend.backend.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/** Teacher management (admin) plus the teacher's own workspace lookups. */
@Service
@Transactional
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final ClasseRepository classeRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    public TeacherService(TeacherRepository teacherRepository,
                          UserRepository userRepository,
                          SubjectRepository subjectRepository,
                          ClasseRepository classeRepository,
                          PasswordEncoder passwordEncoder,
                          CurrentUserService currentUserService) {
        this.teacherRepository = teacherRepository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.classeRepository = classeRepository;
        this.passwordEncoder = passwordEncoder;
        this.currentUserService = currentUserService;
    }

    /** Creates a teacher together with a TEACHER login account. */
    public TeacherResponse create(TeacherRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("email already in use: " + request.getEmail());
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setRole(Role.TEACHER);
        user.setEnabled(true);
        userRepository.save(user);

        Teacher teacher = new Teacher();
        teacher.setNom(request.getNom());
        teacher.setPrenom(request.getPrenom());
        teacher.setUser(user);
        return TeacherMapper.toResponse(teacherRepository.save(teacher));
    }

    @Transactional(readOnly = true)
    public List<TeacherResponse> findAll() {
        return teacherRepository.findAll().stream()
                .map(TeacherMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TeacherResponse findById(Long id) {
        return TeacherMapper.toResponse(getTeacherOrThrow(id));
    }

    /** Updates the teacher's own details (the linked login is kept in step). */
    public TeacherResponse update(Long id, TeacherRequest request) {
        Teacher teacher = getTeacherOrThrow(id);
        teacher.setNom(request.getNom());
        teacher.setPrenom(request.getPrenom());
        User user = teacher.getUser();
        if (user != null) {
            user.setNom(request.getNom());
            user.setPrenom(request.getPrenom());
            userRepository.save(user);
        }
        return TeacherMapper.toResponse(teacherRepository.save(teacher));
    }

    /**
     * Deletes a teacher and their login. Fails with 409 while grades,
     * assessments, attendance or observations still reference them - disable
     * the account instead when there is history to preserve.
     */
    public void delete(Long id) {
        Teacher teacher = getTeacherOrThrow(id);
        User user = teacher.getUser();
        teacherRepository.delete(teacher);
        if (user != null) {
            userRepository.delete(user);
        }
    }

    public TeacherResponse unassignSubject(Long teacherId, Long subjectId) {
        Teacher teacher = getTeacherOrThrow(teacherId);
        teacher.getSubjects().removeIf(s -> s.getId().equals(subjectId));
        return TeacherMapper.toResponse(teacherRepository.save(teacher));
    }

    public TeacherResponse unassignClasse(Long teacherId, Long classeId) {
        Teacher teacher = getTeacherOrThrow(teacherId);
        teacher.getClasses().removeIf(c -> c.getId().equals(classeId));
        return TeacherMapper.toResponse(teacherRepository.save(teacher));
    }

    public TeacherResponse assignSubject(Long teacherId, Long subjectId) {
        Teacher teacher = getTeacherOrThrow(teacherId);
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> ResourceNotFoundException.of("Subject", subjectId));
        teacher.getSubjects().add(subject);
        return TeacherMapper.toResponse(teacherRepository.save(teacher));
    }

    public TeacherResponse assignClasse(Long teacherId, Long classeId) {
        Teacher teacher = getTeacherOrThrow(teacherId);
        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> ResourceNotFoundException.of("Classe", classeId));
        teacher.getClasses().add(classe);
        return TeacherMapper.toResponse(teacherRepository.save(teacher));
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> getMySubjects() {
        return currentUserService.getCurrentTeacher().getSubjects().stream()
                .map(SubjectMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClasseSummaryResponse> getMyClasses() {
        return currentUserService.getCurrentTeacher().getClasses().stream()
                .map(ClasseMapper::toSummary)
                .collect(Collectors.toList());
    }

    private Teacher getTeacherOrThrow(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Teacher", id));
    }
}
