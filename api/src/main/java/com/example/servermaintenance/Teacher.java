package com.example.servermaintenance;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "teacher")
public class Teacher extends AbstractPersistable<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "teacher")
    private List<DataRow> data;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "teacher")
    private List<Course> courses;

    public Teacher(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
