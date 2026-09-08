package com.myga.learning.backend.backend.service;

import com.myga.learning.backend.backend.dto.ParentRequest;
import com.myga.learning.backend.backend.dto.ParentResponse;
import com.myga.learning.backend.backend.dto.StudentSummaryResponse;
import com.myga.learning.backend.backend.exception.ResourceNotFoundException;
import com.myga.learning.backend.backend.mapper.ParentMapper;
import com.myga.learning.backend.backend.mapper.StudentMapper;
import com.myga.learning.backend.backend.models.Parent;
import com.myga.learning.backend.backend.models.Role;
import com.myga.learning.backend.backend.models.Student;
import com.myga.learning.backend.backend.models.User;
import com.myga.learning.backend.backend.repositories.ParentRepository;
import com.myga.learning.backend.backend.repositories.StudentRepository;
import com.myga.learning.backend.backend.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/** Business logic for parents, including optional login provisioning. */
@Service
@Transactional
public class ParentService {

    private final ParentRepository parentRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    public ParentService(ParentRepository parentRepository,
                         StudentRepository studentRepository,
                         UserRepository userRepository,
                         PasswordEncoder passwordEncoder,
                         CurrentUserService currentUserService) {
        this.parentRepository = parentRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<ParentResponse> findAll() {
        return parentRepository.findAll().stream()
                .map(ParentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ParentResponse findByPhone(Long phone) {
        return ParentMapper.toResponse(getParentOrThrow(phone));
    }

    public ParentResponse create(ParentRequest request) {
        Parent parent = new Parent();
        parent.setNom(request.getNom());
        parent.setPrenom(request.getPrenom());
        parent.setEmail(request.getEmail());

        // Optionally provision a PARENT login account (email as username).
        if (StringUtils.hasText(request.getPassword())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("email already in use: " + request.getEmail());
            }
            User user = new User();
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setNom(request.getNom());
            user.setPrenom(request.getPrenom());
            user.setRole(Role.PARENT);
            user.setEnabled(true);
            parent.setUser(userRepository.save(user));
        }
        return ParentMapper.toResponse(parentRepository.save(parent));
    }

    public ParentResponse update(Long phone, ParentRequest request) {
        Parent parent = getParentOrThrow(phone);
        parent.setNom(request.getNom());
        parent.setPrenom(request.getPrenom());
        parent.setEmail(request.getEmail());
        return ParentMapper.toResponse(parentRepository.save(parent));
    }

    public void delete(Long phone) {
        parentRepository.delete(getParentOrThrow(phone));
    }

    /** Links an existing student to a parent as one of their children (admin). */
    public ParentResponse linkStudent(Long phone, Long studentId) {
        Parent parent = getParentOrThrow(phone);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Student", studentId));
        if (parent.getStudents() == null) {
            parent.setStudents(new ArrayList<>());
        }
        boolean alreadyLinked = parent.getStudents().stream()
                .anyMatch(s -> s.getId().equals(student.getId()));
        if (!alreadyLinked) {
            parent.getStudents().add(student);
        }
        return ParentMapper.toResponse(parentRepository.save(parent));
    }

    /** Removes the link between a parent and one of their children (admin). */
    public ParentResponse unlinkStudent(Long phone, Long studentId) {
        Parent parent = getParentOrThrow(phone);
        if (parent.getStudents() != null) {
            parent.getStudents().removeIf(s -> s.getId().equals(studentId));
        }
        return ParentMapper.toResponse(parentRepository.save(parent));
    }

    /** The children linked to the currently authenticated parent. */
    @Transactional(readOnly = true)
    public List<StudentSummaryResponse> getMyChildren() {
        Parent parent = currentUserService.getCurrentParent();
        if (parent.getStudents() == null) {
            return Collections.emptyList();
        }
        return parent.getStudents().stream()
                .map(StudentMapper::toSummary)
                .collect(Collectors.toList());
    }

    private Parent getParentOrThrow(Long phone) {
        return parentRepository.findById(phone)
                .orElseThrow(() -> ResourceNotFoundException.of("Parent", phone));
    }
}
