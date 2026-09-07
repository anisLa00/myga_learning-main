package com.myga.learning.backend.backend.models;

import lombok.*;

import javax.persistence.*;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class Student  {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String nom;
    private String prenom;
    private int Age;

    @ManyToOne
    @JoinColumn(name = "classe_id")
    private Classe Classe;

    public com.myga.learning.backend.backend.models.Classe getClasse() {
        return Classe;
    }

    public void setClasse(com.myga.learning.backend.backend.models.Classe classe) {
        Classe = classe;
    }
    @ManyToMany(mappedBy = "Students")
    private List<Parent> Parents;





    public List<Parent> getParents() {
        return Parents;
    }

    public void setParents(List<Parent> parents) {
        Parents = parents;
    }


    

}
