package com.myga.learning.backend.backend.service;

import com.myga.learning.backend.backend.dto.SemesterAverageResponse;
import com.myga.learning.backend.backend.dto.StudentPerformanceResponse;
import com.myga.learning.backend.backend.dto.SubjectAverageResponse;
import com.myga.learning.backend.backend.exception.ResourceNotFoundException;
import com.myga.learning.backend.backend.mapper.AcademicPeriodMapper;
import com.myga.learning.backend.backend.mapper.StudentMapper;
import com.myga.learning.backend.backend.mapper.SubjectMapper;
import com.myga.learning.backend.backend.models.Grade;
import com.myga.learning.backend.backend.models.PerformanceTrend;
import com.myga.learning.backend.backend.models.Semester;
import com.myga.learning.backend.backend.models.Student;
import com.myga.learning.backend.backend.models.Subject;
import com.myga.learning.backend.backend.repositories.GradeRepository;
import com.myga.learning.backend.backend.repositories.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Academic performance derived from stored grades only.
 *
 * <p>Every grade is normalised to a percentage (value / maxValue) so results on
 * different scales can be averaged together. Nothing is estimated or invented:
 * with no grades the averages are null and the trend is INSUFFICIENT_DATA.
 */
@Service
@Transactional(readOnly = true)
public class PerformanceService {

    /** Minimum grades before a trend is meaningful. */
    private static final int MIN_GRADES_FOR_TREND = 4;
    /** Percentage points of movement before we call it a change. */
    private static final double TREND_THRESHOLD = 2.0;

    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final CurrentUserService currentUserService;

    public PerformanceService(GradeRepository gradeRepository,
                              StudentRepository studentRepository,
                              CurrentUserService currentUserService) {
        this.gradeRepository = gradeRepository;
        this.studentRepository = studentRepository;
        this.currentUserService = currentUserService;
    }

    public StudentPerformanceResponse getStudentPerformance(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Student", studentId));
        currentUserService.ensureCanReadStudent(student);

        List<Grade> grades = gradeRepository.findByStudent_Id(studentId);

        return StudentPerformanceResponse.builder()
                .student(StudentMapper.toSummary(student))
                .totalGrades(grades.size())
                .overallAverage(grades.isEmpty() ? null : round(mean(percentages(grades))))
                .subjectAverages(subjectAverages(grades))
                .semesterAverages(semesterAverages(grades))
                .trend(trend(grades))
                .trendDelta(trendDelta(grades))
                .build();
    }

    // --- aggregation ---

    private List<SubjectAverageResponse> subjectAverages(List<Grade> grades) {
        Map<Long, List<Grade>> bySubject = new LinkedHashMap<>();
        for (Grade g : grades) {
            if (g.getSubject() != null) {
                bySubject.computeIfAbsent(g.getSubject().getId(), k -> new ArrayList<>()).add(g);
            }
        }
        List<SubjectAverageResponse> result = new ArrayList<>();
        for (List<Grade> group : bySubject.values()) {
            Subject subject = group.get(0).getSubject();
            result.add(SubjectAverageResponse.builder()
                    .subject(SubjectMapper.toResponse(subject))
                    .gradeCount(group.size())
                    .average(round(mean(percentages(group))))
                    .build());
        }
        return result;
    }

    private List<SemesterAverageResponse> semesterAverages(List<Grade> grades) {
        Map<Long, List<Grade>> bySemester = new LinkedHashMap<>();
        for (Grade g : grades) {
            if (g.getSemester() != null) {
                bySemester.computeIfAbsent(g.getSemester().getId(), k -> new ArrayList<>()).add(g);
            }
        }
        List<SemesterAverageResponse> result = new ArrayList<>();
        for (List<Grade> group : bySemester.values()) {
            Semester semester = group.get(0).getSemester();
            result.add(SemesterAverageResponse.builder()
                    .semesterId(semester.getId())
                    .semester(semester.getLabel())
                    .academicYear(AcademicPeriodMapper.academicYearLabel(semester))
                    .gradeCount(group.size())
                    .average(round(mean(percentages(group))))
                    .build());
        }
        return result;
    }

    // --- trend ---

    private PerformanceTrend trend(List<Grade> grades) {
        Double delta = trendDelta(grades);
        if (delta == null) {
            return PerformanceTrend.INSUFFICIENT_DATA;
        }
        if (delta > TREND_THRESHOLD) {
            return PerformanceTrend.IMPROVING;
        }
        if (delta < -TREND_THRESHOLD) {
            return PerformanceTrend.DECLINING;
        }
        return PerformanceTrend.STABLE;
    }

    /**
     * Compares the mean of the more recent half of the grades against the
     * earlier half, in percentage points. Null when there is too little data.
     */
    private Double trendDelta(List<Grade> grades) {
        if (grades.size() < MIN_GRADES_FOR_TREND) {
            return null;
        }
        List<Grade> ordered = new ArrayList<>(grades);
        ordered.sort(Comparator.comparing(Grade::getDate, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(Grade::getId, Comparator.nullsFirst(Comparator.naturalOrder())));

        int half = ordered.size() / 2;
        List<Double> earlier = percentages(ordered.subList(0, half));
        List<Double> later = percentages(ordered.subList(ordered.size() - half, ordered.size()));
        return round(mean(later) - mean(earlier));
    }

    // --- helpers ---

    private List<Double> percentages(List<Grade> grades) {
        List<Double> values = new ArrayList<>(grades.size());
        for (Grade g : grades) {
            if (g.getMaxValue() > 0) {
                values.add(g.getValue() / g.getMaxValue() * 100.0);
            }
        }
        return values;
    }

    private double mean(List<Double> values) {
        if (values.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (double v : values) {
            sum += v;
        }
        return sum / values.size();
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
