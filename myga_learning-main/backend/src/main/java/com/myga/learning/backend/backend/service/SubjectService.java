package com.myga.learning.backend.backend.service;

import com.myga.learning.backend.backend.dto.SubjectRequest;
import com.myga.learning.backend.backend.dto.SubjectResponse;
import com.myga.learning.backend.backend.exception.ResourceNotFoundException;
import com.myga.learning.backend.backend.mapper.SubjectMapper;
import com.myga.learning.backend.backend.models.Subject;
import com.myga.learning.backend.backend.repositories.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> findAll() {
        return subjectRepository.findAll().stream()
                .map(SubjectMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SubjectResponse findById(Long id) {
        return subjectRepository.findById(id)
                .map(SubjectMapper::toResponse)
                .orElseThrow(() -> ResourceNotFoundException.of("Subject", id));
    }

    public SubjectResponse create(SubjectRequest request) {
        Subject subject = new Subject();
        subject.setNom(request.getNom());
        subject.setCode(request.getCode());
        return SubjectMapper.toResponse(subjectRepository.save(subject));
    }
}
