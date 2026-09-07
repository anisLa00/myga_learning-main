package com.myga.learning.backend.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Classe {

    @Id
    @GeneratedValue
    private Long id;

    private int salle;

    @OneToMany(mappedBy = "classe")
    @JsonIgnoreProperties({"classe", "parents"})
    private List<Student> students;
}
