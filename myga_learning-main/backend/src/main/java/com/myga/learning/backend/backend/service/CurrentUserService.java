package com.myga.learning.backend.backend.service;

import com.myga.learning.backend.backend.models.Classe;
import com.myga.learning.backend.backend.models.Parent;
import com.myga.learning.backend.backend.models.Student;
import com.myga.learning.backend.backend.models.Teacher;
import com.myga.learning.backend.backend.models.User;
import com.myga.learning.backend.backend.repositories.ParentRepository;
import com.myga.learning.backend.backend.repositories.TeacherRepository;
import com.myga.learning.backend.backend.repositories.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Resolves the currently authenticated principal to the domain records that
 * drive ownership checks. The username in the security context is the user's
 * email (see the JWT setup), which we trust because it came from a validated
 * token, never from a request parameter.
 */
@Service
public class CurrentUserService {

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final ParentRepository parentRepository;

    public CurrentUserService(UserRepository userRepository,
                              TeacherRepository teacherRepository,
                              ParentRepository parentRepository) {
        this.userRepository = userRepository;
        this.teacherRepository = teacherRepository;
        this.parentRepository = parentRepository;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("No authenticated user");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
    }

    public Teacher getCurrentTeacher() {
        String email = getCurrentUser().getEmail();
        return teacherRepository.findByUser_Email(email)
                .orElseThrow(() -> new AccessDeniedException("No teacher profile for the current user"));
    }

    public Parent getCurrentParent() {
        String email = getCurrentUser().getEmail();
        return parentRepository.findByUser_Email(email)
                .orElseThrow(() -> new AccessDeniedException("No parent profile for the current user"));
    }

    public boolean hasRole(String role) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    /**
     * Shared read-ownership rule for a student's academic data (grades,
     * attendance, ...): admins see everyone, a parent only their own children,
     * a teacher only students in their assigned classes. Throws
     * {@link AccessDeniedException} otherwise.
     */
    public void ensureCanReadStudent(Student student) {
        if (hasRole("ADMIN")) {
            return;
        }
        if (hasRole("PARENT")) {
            Parent parent = getCurrentParent();
            boolean ownsChild = parent.getStudents() != null && parent.getStudents().stream()
                    .anyMatch(s -> s.getId().equals(student.getId()));
            if (!ownsChild) {
                throw new AccessDeniedException("This student is not one of your children");
            }
            return;
        }
        if (hasRole("TEACHER")) {
            if (!teacherTeachesStudent(getCurrentTeacher(), student)) {
                throw new AccessDeniedException("This student is not in one of your classes");
            }
            return;
        }
        throw new AccessDeniedException("Not allowed to access this student's data");
    }

    /** True when the student's class is one of the teacher's assigned classes. */
    public boolean teacherTeachesStudent(Teacher teacher, Student student) {
        Classe classe = student.getClasse();
        return classe != null && teacherTeachesClasse(teacher, classe.getId());
    }

    /** True when the teacher is assigned to the given class. */
    public boolean teacherTeachesClasse(Teacher teacher, Long classeId) {
        return classeId != null && teacher.getClasses().stream()
                .anyMatch(c -> c.getId().equals(classeId));
    }
}
