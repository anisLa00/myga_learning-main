package com.myga.learning.backend.backend.controller;

import com.myga.learning.backend.backend.models.Parent;

import com.myga.learning.backend.backend.repositories.ParentRepository;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
public class ParentController {


    @Autowired
    ParentRepository ParentRepository;



    @GetMapping("/Parents")
    public List<Parent> findAll(){
        return this.ParentRepository.findAll();

    }
    @GetMapping("/Parents/{phone}")
    public Parent findById(@PathVariable Long phone) throws Exception{
        return (Parent) this.ParentRepository.findById(phone).orElseThrow(()-> new Exception("n'existe pas"));

    }
    @PostMapping("/Parents")
    public Parent saveParent(@RequestBody Parent Parent){
        return this.ParentRepository.save(Parent);
    }

    @PutMapping("/Parents/{phone}")
    Parent updateOrSaveParent(@RequestBody Parent Parent, @PathVariable Long phone) {
        return this.ParentRepository.findById(phone).map(x->{
            x.setNom(Parent.getNom());
            x.setPrenom(Parent.getPrenom());
            x.setEmail(Parent.getEmail());
            return ParentRepository.save(x);
        }).orElseGet(()->{
            Parent.setPhone(phone);
            return ParentRepository.save(Parent);
        });
    }
    @DeleteMapping("/Parents/{phone}")
    void deleteParent(@PathVariable Long phone) {
        this.ParentRepository.deleteById(phone);
    }



    }






