package com.myga.learning.backend.backend.controller;

import com.myga.learning.backend.backend.models.Classe;
import com.myga.learning.backend.backend.repositories.ClasseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ClasseController {
    @Autowired
    ClasseRepository ClasseRepository;



    @GetMapping("/Classes")
    public List<Classe> findAll(){
        return this.ClasseRepository.findAll();

    }
    @GetMapping("/Classes/{id}")
    public Classe findById(@PathVariable Long id) throws Exception{
        return (Classe) this.ClasseRepository.findById(id).orElseThrow(()-> new Exception("n'existe pas"));

    }

}
