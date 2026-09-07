package com.myga.learning.backend.backend.service;

import com.myga.learning.backend.backend.dto.ParentRequest;
import com.myga.learning.backend.backend.dto.ParentResponse;
import com.myga.learning.backend.backend.exception.ResourceNotFoundException;
import com.myga.learning.backend.backend.mapper.ParentMapper;
import com.myga.learning.backend.backend.models.Parent;
import com.myga.learning.backend.backend.repositories.ParentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/** Business logic for parents. */
@Service
@Transactional
public class ParentService {

    private final ParentRepository parentRepository;

    public ParentService(ParentRepository parentRepository) {
        this.parentRepository = parentRepository;
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
        applyRequest(parent, request);
        return ParentMapper.toResponse(parentRepository.save(parent));
    }

    public ParentResponse update(Long phone, ParentRequest request) {
        Parent parent = getParentOrThrow(phone);
        applyRequest(parent, request);
        return ParentMapper.toResponse(parentRepository.save(parent));
    }

    public void delete(Long phone) {
        parentRepository.delete(getParentOrThrow(phone));
    }

    private void applyRequest(Parent parent, ParentRequest request) {
        parent.setNom(request.getNom());
        parent.setPrenom(request.getPrenom());
        parent.setEmail(request.getEmail());
    }

    private Parent getParentOrThrow(Long phone) {
        return parentRepository.findById(phone)
                .orElseThrow(() -> ResourceNotFoundException.of("Parent", phone));
    }
}
