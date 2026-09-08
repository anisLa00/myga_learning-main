package com.myga.learning.backend.backend.service;

import com.myga.learning.backend.backend.dto.AcademicYearRequest;
import com.myga.learning.backend.backend.dto.AcademicYearResponse;
import com.myga.learning.backend.backend.dto.SemesterRequest;
import com.myga.learning.backend.backend.dto.SemesterResponse;
import com.myga.learning.backend.backend.exception.ResourceNotFoundException;
import com.myga.learning.backend.backend.mapper.AcademicPeriodMapper;
import com.myga.learning.backend.backend.models.AcademicYear;
import com.myga.learning.backend.backend.models.Semester;
import com.myga.learning.backend.backend.repositories.AcademicYearRepository;
import com.myga.learning.backend.backend.repositories.SemesterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/** Academic years and their semesters (administrator configuration). */
@Service
@Transactional
public class AcademicPeriodService {

    private final AcademicYearRepository academicYearRepository;
    private final SemesterRepository semesterRepository;

    public AcademicPeriodService(AcademicYearRepository academicYearRepository,
                                 SemesterRepository semesterRepository) {
        this.academicYearRepository = academicYearRepository;
        this.semesterRepository = semesterRepository;
    }

    // --- academic years ---

    public AcademicYearResponse createYear(AcademicYearRequest request) {
        if (academicYearRepository.existsByLabel(request.getLabel())) {
            throw new IllegalArgumentException("An academic year already exists with label " + request.getLabel());
        }
        AcademicYear year = new AcademicYear();
        year.setLabel(request.getLabel());
        year.setStartDate(request.getStartDate());
        year.setEndDate(request.getEndDate());
        year.setCurrent(Boolean.TRUE.equals(request.getCurrent()));
        if (year.isCurrent()) {
            clearCurrentFlag();
        }
        return AcademicPeriodMapper.toResponse(academicYearRepository.save(year));
    }

    /** Makes one year the current one, clearing the flag on any other. */
    public AcademicYearResponse setCurrentYear(Long id) {
        AcademicYear year = getYearOrThrow(id);
        clearCurrentFlag();
        year.setCurrent(true);
        return AcademicPeriodMapper.toResponse(academicYearRepository.save(year));
    }

    @Transactional(readOnly = true)
    public List<AcademicYearResponse> findYears() {
        return academicYearRepository.findAll().stream()
                .map(AcademicPeriodMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AcademicYearResponse findYear(Long id) {
        return AcademicPeriodMapper.toResponse(getYearOrThrow(id));
    }

    // --- semesters ---

    public SemesterResponse createSemester(SemesterRequest request) {
        AcademicYear year = getYearOrThrow(request.getAcademicYearId());
        Semester semester = new Semester();
        semester.setLabel(request.getLabel());
        semester.setAcademicYear(year);
        semester.setStartDate(request.getStartDate());
        semester.setEndDate(request.getEndDate());
        return AcademicPeriodMapper.toResponse(semesterRepository.save(semester));
    }

    @Transactional(readOnly = true)
    public List<SemesterResponse> findSemesters(Long academicYearId) {
        List<Semester> semesters = academicYearId == null
                ? semesterRepository.findAll()
                : semesterRepository.findByAcademicYear_IdOrderByStartDateAsc(academicYearId);
        return semesters.stream()
                .map(AcademicPeriodMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SemesterResponse findSemester(Long id) {
        return AcademicPeriodMapper.toResponse(getSemesterOrThrow(id));
    }

    // --- shared lookups ---

    public AcademicYear getYearOrThrow(Long id) {
        return academicYearRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("AcademicYear", id));
    }

    public Semester getSemesterOrThrow(Long id) {
        return semesterRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Semester", id));
    }

    private void clearCurrentFlag() {
        List<AcademicYear> current = academicYearRepository.findAllByCurrentTrue();
        current.forEach(y -> y.setCurrent(false));
        academicYearRepository.saveAll(current);
    }
}
