package com.myga.learning.backend.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String nom;
    private String prenom;
    private int age;

    @ManyToOne
    @JoinColumn(name = "classe_id")
    @JsonIgnoreProperties("students")
    private Classe classe;

    @ManyToMany(mappedBy = "students")
    @JsonIgnoreProperties("students")
    private List<Parent> parents;
}
