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
public class Parent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long phone;

    private String nom;
    private String prenom;
    private String email;

    @ManyToMany
    @JoinTable(name = "PARENT_STUD",
            joinColumns = @JoinColumn(name = "PARENT_PHONE"),
            inverseJoinColumns = @JoinColumn(name = "STUDENT_ID"))
    @JsonIgnoreProperties({"classe", "parents"})
    private List<Student> students;
}
