package com.myga.learning.backend.backend.models;


import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class Classe {
    @Id
    @GeneratedValue
    private Long id;
    private int salle;

    @OneToMany (mappedBy = "Classe")


    private List<Student> Students;


    public List<Student> getStudents() {
        return Students;
    }

    public void setStudents(List<Student> students) {
        Students = students;
    }
}
