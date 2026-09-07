package com.myga.learning.backend.backend.controller;

import com.myga.learning.backend.backend.models.Student;
import com.myga.learning.backend.backend.repositories.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class StudentController {


    @Autowired
    StudentRepository StudentRepository;



    @GetMapping("/Students")
    public List<Student> findAll(){
        return this.StudentRepository.findAll();

    }

    @GetMapping("/Students/{id}")
    public  Student findById( @PathVariable Long id) throws Exception{
        return this.StudentRepository.findById(id).orElseThrow(()-> new Exception("n'existe pas"));

    }
    @PostMapping("/Students")
    public Student saveStudent(@RequestBody Student Student){
        return this.StudentRepository.save(Student);
    }
    @PutMapping("/Students/{id}")
     Student updateOrSaveStudent(@RequestBody Student Student, @PathVariable Long id) {
        return this.StudentRepository.findById(id).map(x->{
            x.setNom(Student.getNom());
            x.setPrenom(Student.getPrenom());
            x.setAge(Student.getAge());
            return StudentRepository.save(x);
        }).orElseGet(()->{
            Student.setId(id);
            return StudentRepository.save(Student);
        });
    }
    @DeleteMapping("/Students/{id}")
    void deleteStudent(@PathVariable Long id) {
        this.StudentRepository.deleteById(id);
    }
}
