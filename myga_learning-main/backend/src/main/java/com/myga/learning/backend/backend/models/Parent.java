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
public class Parent<inverseJoinColumns> {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long phone;
    private String nom;
    private String prenom;
    private String Email;


    @ManyToMany
    @JoinTable(name = "PARENT_STUD" ,
	joinColumns = @JoinColumn(name = "PARENT_PHONE"),
	inverseJoinColumns = @JoinColumn(name = "STUDENT_ID"))
	private List<Student> Students;

	public List<Student> getStudents() {
		return Students;
	}

	public void setStudents(List<Student> students) {
		Students = students;
	}

	public Long getPhone() {
		return phone;
	}

	public void setPhone(Long phone) {
		this.phone = phone;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getPrenom() {
		return prenom;
	}

	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}

	public String getEmail() {
		return Email;
	}

	public void setEmail(String email) {
		Email = email;
	}
    
}
