package com.myga.learning.backend.backend.service;

import com.myga.learning.backend.backend.dto.ClasseRequest;
import com.myga.learning.backend.backend.dto.ClasseResponse;
import com.myga.learning.backend.backend.exception.ResourceNotFoundException;
import com.myga.learning.backend.backend.mapper.ClasseMapper;
import com.myga.learning.backend.backend.models.Classe;
import com.myga.learning.backend.backend.repositories.ClasseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/** Class management: read for everyone authenticated, create for admins. */
@Service
@Transactional(readOnly = true)
public class ClasseService {

    private final ClasseRepository classeRepository;

    public ClasseService(ClasseRepository classeRepository) {
        this.classeRepository = classeRepository;
    }

    @Transactional
    public ClasseResponse create(ClasseRequest request) {
        Classe classe = new Classe();
        classe.setSalle(request.getSalle());
        return ClasseMapper.toResponse(classeRepository.save(classe));
    }

    @Transactional
    public ClasseResponse update(Long id, ClasseRequest request) {
        Classe classe = classeRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Classe", id));
        classe.setSalle(request.getSalle());
        return ClasseMapper.toResponse(classeRepository.save(classe));
    }

    /** Fails with 409 if students or records still reference the class. */
    @Transactional
    public void delete(Long id) {
        classeRepository.delete(classeRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Classe", id)));
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
