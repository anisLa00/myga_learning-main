package com.myga.learning.backend.backend.service;

import com.myga.learning.backend.backend.dto.StudentRequest;
import com.myga.learning.backend.backend.dto.StudentResponse;
import com.myga.learning.backend.backend.exception.ResourceNotFoundException;
import com.myga.learning.backend.backend.mapper.StudentMapper;
import com.myga.learning.backend.backend.models.Classe;
import com.myga.learning.backend.backend.models.Student;
import com.myga.learning.backend.backend.repositories.ClasseRepository;
import com.myga.learning.backend.backend.repositories.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/** Business logic for students. Mapping runs inside the transaction so lazy
 *  associations can be resolved before the entity is converted to a DTO. */
@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final ClasseRepository classeRepository;

    public StudentService(StudentRepository studentRepository, ClasseRepository classeRepository) {
        this.studentRepository = studentRepository;
        this.classeRepository = classeRepository;
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> findAll() {
        return studentRepository.findAll().stream()
                .map(StudentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StudentResponse findById(Long id) {
        return StudentMapper.toResponse(getStudentOrThrow(id));
    }

    public StudentResponse create(StudentRequest request) {
        Student student = new Student();
        applyRequest(student, request);
        return StudentMapper.toResponse(studentRepository.save(student));
    }

    public StudentResponse update(Long id, StudentRequest request) {
        Student student = getStudentOrThrow(id);
        applyRequest(student, request);
        return StudentMapper.toResponse(studentRepository.save(student));
    }

    public void delete(Long id) {
        studentRepository.delete(getStudentOrThrow(id));
    }

    private void applyRequest(Student student, StudentRequest request) {
        student.setNom(request.getNom());
        student.setPrenom(request.getPrenom());
        student.setAge(request.getAge());
        if (request.getClasseId() != null) {
            Classe classe = classeRepository.findById(request.getClasseId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Classe", request.getClasseId()));
            student.setClasse(classe);
        } else {
            student.setClasse(null);
        }
    }

    private Student getStudentOrThrow(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Student", id));
    }
}
