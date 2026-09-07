package com.myga.learning.backend.backend.service;

import com.myga.learning.backend.backend.dto.ClasseResponse;
import com.myga.learning.backend.backend.exception.ResourceNotFoundException;
import com.myga.learning.backend.backend.mapper.ClasseMapper;
import com.myga.learning.backend.backend.repositories.ClasseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/** Read-only business logic for classes (management endpoints come later). */
@Service
@Transactional(readOnly = true)
public class ClasseService {

    private final ClasseRepository classeRepository;

    public ClasseService(ClasseRepository classeRepository) {
        this.classeRepository = classeRepository;
    }

    public List<ClasseResponse> findAll() {
        return classeRepository.findAll().stream()
                .map(ClasseMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ClasseResponse findById(Long id) {
        return classeRepository.findById(id)
                .map(ClasseMapper::toResponse)
                .orElseThrow(() -> ResourceNotFoundException.of("Classe", id));
    }
}
