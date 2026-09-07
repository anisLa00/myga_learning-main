package com.myga.learning.backend.backend.service;

import com.myga.learning.backend.backend.dto.AnnouncementRequest;
import com.myga.learning.backend.backend.dto.AnnouncementResponse;
import com.myga.learning.backend.backend.exception.ResourceNotFoundException;
import com.myga.learning.backend.backend.mapper.AnnouncementMapper;
import com.myga.learning.backend.backend.models.Announcement;
import com.myga.learning.backend.backend.models.AnnouncementTarget;
import com.myga.learning.backend.backend.models.Classe;
import com.myga.learning.backend.backend.models.NotificationType;
import com.myga.learning.backend.backend.models.Parent;
import com.myga.learning.backend.backend.models.Role;
import com.myga.learning.backend.backend.models.Student;
import com.myga.learning.backend.backend.models.Teacher;
import com.myga.learning.backend.backend.models.User;
import com.myga.learning.backend.backend.repositories.AnnouncementRepository;
import com.myga.learning.backend.backend.repositories.ClasseRepository;
import com.myga.learning.backend.backend.repositories.ParentRepository;
import com.myga.learning.backend.backend.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Publishes announcements, fans out notifications, and serves per-user feeds. */
@Service
@Transactional
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final ClasseRepository classeRepository;
    private final UserRepository userRepository;
    private final ParentRepository parentRepository;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;

    public AnnouncementService(AnnouncementRepository announcementRepository,
                               ClasseRepository classeRepository,
                               UserRepository userRepository,
                               ParentRepository parentRepository,
                               CurrentUserService currentUserService,
                               NotificationService notificationService) {
        this.announcementRepository = announcementRepository;
        this.classeRepository = classeRepository;
        this.userRepository = userRepository;
        this.parentRepository = parentRepository;
        this.currentUserService = currentUserService;
        this.notificationService = notificationService;
    }

    public AnnouncementResponse create(AnnouncementRequest request) {
        Announcement announcement = new Announcement();
        announcement.setTitle(request.getTitle());
        announcement.setMessage(request.getMessage());
        announcement.setTarget(request.getTarget());
        announcement.setCreatedBy(currentUserService.getCurrentUser());
        announcement.setCreatedAt(LocalDateTime.now());

        if (request.getTarget() == AnnouncementTarget.CLASS) {
            if (request.getClasseId() == null) {
                throw new IllegalArgumentException("classeId is required for a CLASS announcement");
            }
            announcement.setTargetClasse(getClasse(request.getClasseId()));
        }
        if (request.getTarget() == AnnouncementTarget.USER) {
            if (request.getUserId() == null) {
                throw new IllegalArgumentException("userId is required for a USER announcement");
            }
            announcement.setTargetUser(userRepository.findById(request.getUserId())
                    .orElseThrow(() -> ResourceNotFoundException.of("User", request.getUserId())));
        }

        Announcement saved = announcementRepository.save(announcement);
        fanOutNotifications(saved);
        return AnnouncementMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AnnouncementResponse> findAll() {
        return announcementRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(AnnouncementMapper::toResponse)
                .collect(Collectors.toList());
    }

    /** The announcements relevant to the currently authenticated user. */
    @Transactional(readOnly = true)
    public List<AnnouncementResponse> findForCurrentUser() {
        User user = currentUserService.getCurrentUser();
        return announcementRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(a -> isVisibleTo(a, user))
                .map(AnnouncementMapper::toResponse)
                .collect(Collectors.toList());
    }

    private boolean isVisibleTo(Announcement a, User user) {
        switch (a.getTarget()) {
            case ALL:
                return true;
            case PARENTS:
                return user.getRole() == Role.PARENT;
            case TEACHERS:
                return user.getRole() == Role.TEACHER;
            case USER:
                return a.getTargetUser() != null && a.getTargetUser().getId().equals(user.getId());
            case CLASS:
                return a.getTargetClasse() != null && userBelongsToClass(user, a.getTargetClasse().getId());
            default:
                return false;
        }
    }

    private boolean userBelongsToClass(User user, Long classeId) {
        if (user.getRole() == Role.PARENT && currentUserService.hasRole("PARENT")) {
            Parent parent = currentUserService.getCurrentParent();
            return parent.getStudents() != null && parent.getStudents().stream()
                    .map(Student::getClasse)
                    .filter(c -> c != null)
                    .anyMatch(c -> c.getId().equals(classeId));
        }
        if (user.getRole() == Role.TEACHER && currentUserService.hasRole("TEACHER")) {
            Teacher teacher = currentUserService.getCurrentTeacher();
            return currentUserService.teacherTeachesClasse(teacher, classeId);
        }
        return false;
    }

    private void fanOutNotifications(Announcement a) {
        List<User> recipients = resolveRecipients(a);
        String title = "New announcement: " + a.getTitle();
        for (User recipient : recipients) {
            notificationService.notifyUser(recipient, NotificationType.NEW_ANNOUNCEMENT, title, a.getMessage());
        }
    }

    private List<User> resolveRecipients(Announcement a) {
        switch (a.getTarget()) {
            case ALL:
                return userRepository.findAll();
            case PARENTS:
                return userRepository.findByRole(Role.PARENT);
            case TEACHERS:
                return userRepository.findByRole(Role.TEACHER);
            case USER:
                return a.getTargetUser() == null ? List.of() : List.of(a.getTargetUser());
            case CLASS:
                List<User> users = new ArrayList<>();
                Set<Long> seen = new java.util.HashSet<>();
                for (Parent parent : parentRepository.findDistinctByStudents_Classe_Id(a.getTargetClasse().getId())) {
                    User u = parent.getUser();
                    if (u != null && seen.add(u.getId())) {
                        users.add(u);
                    }
                }
                return users;
            default:
                return List.of();
        }
    }

    private Classe getClasse(Long classeId) {
        return classeRepository.findById(classeId)
                .orElseThrow(() -> ResourceNotFoundException.of("Classe", classeId));
    }
}
