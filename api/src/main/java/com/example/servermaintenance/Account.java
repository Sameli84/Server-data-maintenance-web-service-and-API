package com.example.servermaintenance;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "account")
public class Account extends AbstractPersistable<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @OneToMany(mappedBy = "account")
    private List<DataRow> data;

    @OneToMany(mappedBy = "account")
    private List<Course> courses;

    @ManyToMany(mappedBy = "students")
    private Set<Course> studentCourses = new HashSet<>();

    public Account(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
}
