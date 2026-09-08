package com.myga.learning.backend.backend.service;

import com.myga.learning.backend.backend.dto.AdminDashboardResponse;
import com.myga.learning.backend.backend.dto.AnnouncementResponse;
import com.myga.learning.backend.backend.dto.ChildAttendanceStatsResponse;
import com.myga.learning.backend.backend.dto.ParentDashboardResponse;
import com.myga.learning.backend.backend.dto.TeacherDashboardResponse;
import com.myga.learning.backend.backend.mapper.AttendanceMapper;
import com.myga.learning.backend.backend.mapper.ClasseMapper;
import com.myga.learning.backend.backend.mapper.GradeMapper;
import com.myga.learning.backend.backend.mapper.ObservationMapper;
import com.myga.learning.backend.backend.mapper.StudentMapper;
import com.myga.learning.backend.backend.mapper.SubjectMapper;
import com.myga.learning.backend.backend.models.Attendance;
import com.myga.learning.backend.backend.models.AttendanceStatus;
import com.myga.learning.backend.backend.models.Classe;
import com.myga.learning.backend.backend.models.Parent;
import com.myga.learning.backend.backend.models.Student;
import com.myga.learning.backend.backend.models.Teacher;
import com.myga.learning.backend.backend.repositories.AttendanceRepository;
import com.myga.learning.backend.backend.repositories.ClasseRepository;
import com.myga.learning.backend.backend.repositories.GradeRepository;
import com.myga.learning.backend.backend.repositories.ObservationRepository;
import com.myga.learning.backend.backend.repositories.ParentRepository;
import com.myga.learning.backend.backend.repositories.StudentRepository;
import com.myga.learning.backend.backend.repositories.SubjectRepository;
import com.myga.learning.backend.backend.repositories.TeacherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Read-only aggregates powering the three role dashboards. Every figure is
 * derived from stored data; nothing is invented. The teacher and parent views
 * are scoped to the caller's own assignments / children.
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final int FEED_LIMIT = 5;

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final ParentRepository parentRepository;
    private final ClasseRepository classeRepository;
    private final SubjectRepository subjectRepository;
    private final GradeRepository gradeRepository;
    private final AttendanceRepository attendanceRepository;
    private final ObservationRepository observationRepository;
    private final CurrentUserService currentUserService;
    private final AnnouncementService announcementService;
    private final NotificationService notificationService;

    public DashboardService(StudentRepository studentRepository,
                            TeacherRepository teacherRepository,
                            ParentRepository parentRepository,
                            ClasseRepository classeRepository,
                            SubjectRepository subjectRepository,
                            GradeRepository gradeRepository,
                            AttendanceRepository attendanceRepository,
                            ObservationRepository observationRepository,
                            CurrentUserService currentUserService,
                            AnnouncementService announcementService,
                            NotificationService notificationService) {
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.parentRepository = parentRepository;
        this.classeRepository = classeRepository;
        this.subjectRepository = subjectRepository;
        this.gradeRepository = gradeRepository;
        this.attendanceRepository = attendanceRepository;
        this.observationRepository = observationRepository;
        this.currentUserService = currentUserService;
        this.announcementService = announcementService;
        this.notificationService = notificationService;
    }

    public AdminDashboardResponse adminDashboard() {
        return AdminDashboardResponse.builder()
                .totalStudents(studentRepository.count())
                .totalTeachers(teacherRepository.count())
                .totalParents(parentRepository.count())
                .totalClasses(classeRepository.count())
                .totalSubjects(subjectRepository.count())
                .recentGrades(gradeRepository.findTop5ByOrderByDateDescIdDesc().stream()
                        .map(GradeMapper::toResponse).collect(Collectors.toList()))
                .recentAttendance(attendanceRepository.findTop5ByOrderByDateDescIdDesc().stream()
                        .map(AttendanceMapper::toResponse).collect(Collectors.toList()))
                .recentAbsences(attendanceRepository
                        .findTop5ByStatusOrderByDateDescIdDesc(AttendanceStatus.ABSENT).stream()
                        .map(AttendanceMapper::toResponse).collect(Collectors.toList()))
                .recentAnnouncements(limit(announcementService.findAll()))
                .build();
    }

    public TeacherDashboardResponse teacherDashboard() {
        Teacher teacher = currentUserService.getCurrentTeacher();
        List<Long> classeIds = teacher.getClasses().stream()
                .map(Classe::getId)
                .collect(Collectors.toList());

        return TeacherDashboardResponse.builder()
                .classes(teacher.getClasses().stream()
                        .map(ClasseMapper::toSummary).collect(Collectors.toList()))
                .subjects(teacher.getSubjects().stream()
                        .map(SubjectMapper::toResponse).collect(Collectors.toList()))
                .totalStudents(classeIds.isEmpty() ? 0 : studentRepository.countByClasse_IdIn(classeIds))
                .recentGrades(gradeRepository
                        .findTop5ByTeacher_IdOrderByDateDescIdDesc(teacher.getId()).stream()
                        .map(GradeMapper::toResponse).collect(Collectors.toList()))
                .recentObservations(observationRepository
                        .findTop5ByTeacher_IdOrderByDateDescIdDesc(teacher.getId()).stream()
                        .map(ObservationMapper::toResponse).collect(Collectors.toList()))
                .announcements(limit(announcementService.findForCurrentUser()))
                .unreadNotifications(notificationService.unreadCountMine())
                .build();
    }

    public ParentDashboardResponse parentDashboard() {
        Parent parent = currentUserService.getCurrentParent();
        List<Student> children = parent.getStudents() == null
                ? Collections.emptyList()
                : parent.getStudents();
        List<Long> childIds = children.stream().map(Student::getId).collect(Collectors.toList());

        ParentDashboardResponse.ParentDashboardResponseBuilder builder = ParentDashboardResponse.builder()
                .children(children.stream().map(StudentMapper::toSummary).collect(Collectors.toList()))
                .attendance(children.stream().map(this::attendanceStats).collect(Collectors.toList()))
                .announcements(limit(announcementService.findForCurrentUser()))
                .unreadNotifications(notificationService.unreadCountMine());

        if (childIds.isEmpty()) {
            return builder
                    .latestGrades(Collections.emptyList())
                    .recentAbsences(Collections.emptyList())
                    .recentFeedback(Collections.emptyList())
                    .build();
        }

        return builder
                .latestGrades(gradeRepository.findTop5ByStudent_IdInOrderByDateDescIdDesc(childIds).stream()
                        .map(GradeMapper::toResponse).collect(Collectors.toList()))
                .recentAbsences(attendanceRepository
                        .findTop5ByStudent_IdInAndStatusOrderByDateDescIdDesc(childIds, AttendanceStatus.ABSENT)
                        .stream().map(AttendanceMapper::toResponse).collect(Collectors.toList()))
                .recentFeedback(observationRepository
                        .findTop5ByStudent_IdInAndVisibleToParentsTrueOrderByDateDescIdDesc(childIds)
                        .stream().map(ObservationMapper::toResponse).collect(Collectors.toList()))
                .build();
    }

    private ChildAttendanceStatsResponse attendanceStats(Student child) {
        List<Attendance> records = attendanceRepository.findByStudent_IdOrderByDateDesc(child.getId());
        int present = count(records, AttendanceStatus.PRESENT);
        int absent = count(records, AttendanceStatus.ABSENT);
        int late = count(records, AttendanceStatus.LATE);
        int excused = count(records, AttendanceStatus.EXCUSED);
        int total = records.size();
        // PRESENT and LATE count as attended, consistent with the attendance endpoint.
        double percentage = total == 0 ? 0.0 : Math.round((present + late) * 1000.0 / total) / 10.0;

        return ChildAttendanceStatsResponse.builder()
                .student(StudentMapper.toSummary(child))
                .total(total)
                .totalPresent(present)
                .totalAbsent(absent)
                .totalLate(late)
                .totalExcused(excused)
                .attendancePercentage(percentage)
                .build();
    }

    private int count(List<Attendance> records, AttendanceStatus status) {
        return (int) records.stream().filter(a -> a.getStatus() == status).count();
    }

    private List<AnnouncementResponse> limit(List<AnnouncementResponse> announcements) {
        return announcements.stream().limit(FEED_LIMIT).collect(Collectors.toList());
    }
}
