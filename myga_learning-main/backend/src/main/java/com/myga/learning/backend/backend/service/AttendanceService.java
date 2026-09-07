package com.myga.learning.backend.backend.service;

import com.myga.learning.backend.backend.dto.AttendanceBulkRequest;
import com.myga.learning.backend.backend.dto.AttendanceRequest;
import com.myga.learning.backend.backend.dto.AttendanceResponse;
import com.myga.learning.backend.backend.dto.AttendanceSummaryResponse;
import com.myga.learning.backend.backend.exception.ResourceNotFoundException;
import com.myga.learning.backend.backend.mapper.AttendanceMapper;
import com.myga.learning.backend.backend.models.Attendance;
import com.myga.learning.backend.backend.models.AttendanceStatus;
import com.myga.learning.backend.backend.models.Classe;
import com.myga.learning.backend.backend.models.NotificationType;
import com.myga.learning.backend.backend.models.Student;
import com.myga.learning.backend.backend.models.Subject;
import com.myga.learning.backend.backend.models.Teacher;
import com.myga.learning.backend.backend.repositories.AttendanceRepository;
import com.myga.learning.backend.backend.repositories.ClasseRepository;
import com.myga.learning.backend.backend.repositories.StudentRepository;
import com.myga.learning.backend.backend.repositories.SubjectRepository;
import com.myga.learning.backend.backend.repositories.TeacherRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Attendance recording and retrieval with the same ownership rules as grades:
 * a teacher may only mark their own classes, and a student's records are
 * readable only by an admin, the student's parent, or a teacher of their class.
 */
@Service
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final ClasseRepository classeRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             StudentRepository studentRepository,
                             ClasseRepository classeRepository,
                             SubjectRepository subjectRepository,
                             TeacherRepository teacherRepository,
                             CurrentUserService currentUserService,
                             NotificationService notificationService) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.classeRepository = classeRepository;
        this.subjectRepository = subjectRepository;
        this.teacherRepository = teacherRepository;
        this.currentUserService = currentUserService;
        this.notificationService = notificationService;
    }

    public AttendanceResponse record(AttendanceRequest request) {
        Classe classe = getClasse(request.getClasseId());
        Teacher teacher = resolveTeacherForClass(request.getTeacherId(), classe);
        Subject subject = resolveSubject(request.getSubjectId());
        Student student = getStudentInClass(request.getStudentId(), classe);

        Attendance attendance = build(student, classe, teacher, subject,
                request.getDate(), request.getStatus(), request.getNote());
        Attendance saved = attendanceRepository.save(attendance);
        notifyIfAbsent(saved);
        return AttendanceMapper.toResponse(saved);
    }

    public List<AttendanceResponse> markClass(AttendanceBulkRequest request) {
        Classe classe = getClasse(request.getClasseId());
        Teacher teacher = resolveTeacherForClass(request.getTeacherId(), classe);
        Subject subject = resolveSubject(request.getSubjectId());
        LocalDate date = request.getDate() != null ? request.getDate() : LocalDate.now();

        List<Attendance> toSave = new ArrayList<>();
        for (AttendanceBulkRequest.Entry entry : request.getEntries()) {
            Student student = getStudentInClass(entry.getStudentId(), classe);
            toSave.add(build(student, classe, teacher, subject, date, entry.getStatus(), entry.getNote()));
        }
        List<Attendance> saved = attendanceRepository.saveAll(toSave);
        saved.forEach(this::notifyIfAbsent);
        return saved.stream()
                .map(AttendanceMapper::toResponse)
                .collect(Collectors.toList());
    }

    /** Notify the student's parents when they are marked absent. */
    private void notifyIfAbsent(Attendance attendance) {
        if (attendance.getStatus() != AttendanceStatus.ABSENT) {
            return;
        }
        Student student = attendance.getStudent();
        notificationService.notifyStudentParents(student, NotificationType.NEW_ABSENCE,
                "Absence recorded",
                student.getPrenom() + " " + student.getNom()
                        + " was marked absent on " + attendance.getDate() + ".");
    }

    @Transactional(readOnly = true)
    public AttendanceSummaryResponse getStudentAttendance(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Student", studentId));
        currentUserService.ensureCanReadStudent(student);

        List<Attendance> records = attendanceRepository.findByStudent_IdOrderByDateDesc(studentId);
        int present = count(records, AttendanceStatus.PRESENT);
        int absent = count(records, AttendanceStatus.ABSENT);
        int late = count(records, AttendanceStatus.LATE);
        int excused = count(records, AttendanceStatus.EXCUSED);
        int total = records.size();
        double percentage = total == 0 ? 0.0
                : Math.round((present + late) * 1000.0 / total) / 10.0;

        return AttendanceSummaryResponse.builder()
                .records(records.stream().map(AttendanceMapper::toResponse).collect(Collectors.toList()))
                .total(total)
                .totalPresent(present)
                .totalAbsent(absent)
                .totalLate(late)
                .totalExcused(excused)
                .attendancePercentage(percentage)
                .build();
    }

    // --- helpers ---

    private Attendance build(Student student, Classe classe, Teacher teacher, Subject subject,
                             LocalDate date, AttendanceStatus status, String note) {
        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setClasse(classe);
        attendance.setTeacher(teacher);
        attendance.setSubject(subject);
        attendance.setDate(date != null ? date : LocalDate.now());
        attendance.setStatus(status);
        attendance.setNote(note);
        return attendance;
    }

    private Classe getClasse(Long classeId) {
        return classeRepository.findById(classeId)
                .orElseThrow(() -> ResourceNotFoundException.of("Classe", classeId));
    }

    private Subject resolveSubject(Long subjectId) {
        if (subjectId == null) {
            return null;
        }
        return subjectRepository.findById(subjectId)
                .orElseThrow(() -> ResourceNotFoundException.of("Subject", subjectId));
    }

    private Student getStudentInClass(Long studentId, Classe classe) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Student", studentId));
        if (student.getClasse() == null || !student.getClasse().getId().equals(classe.getId())) {
            throw new IllegalArgumentException("Student " + studentId + " is not in class " + classe.getId());
        }
        return student;
    }

    private Teacher resolveTeacherForClass(Long requestedTeacherId, Classe classe) {
        if (currentUserService.hasRole("TEACHER")) {
            Teacher teacher = currentUserService.getCurrentTeacher();
            if (!currentUserService.teacherTeachesClasse(teacher, classe.getId())) {
                throw new AccessDeniedException("This class is not one of your assigned classes");
            }
            return teacher;
        }
        if (requestedTeacherId == null) {
            throw new IllegalArgumentException("teacherId is required when an admin records attendance");
        }
        return teacherRepository.findById(requestedTeacherId)
                .orElseThrow(() -> ResourceNotFoundException.of("Teacher", requestedTeacherId));
    }

    private int count(List<Attendance> records, AttendanceStatus status) {
        return (int) records.stream().filter(a -> a.getStatus() == status).count();
    }
}
