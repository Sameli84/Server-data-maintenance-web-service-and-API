package com.example.servermaintenance.course;

import com.example.servermaintenance.account.Account;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "course")
public class Course extends AbstractPersistable<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "url", unique = true)
    private String url;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private Account owner;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CourseKey> courseKeys = new HashSet<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CourseStudent> courseStudents = new HashSet<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SchemaPart> schemaParts = new HashSet<>();

    @OneToOne(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @PrimaryKeyJoinColumn
    private CourseIndex courseIndex;

    public Course(String name, String url, Account account) {
        this.name = name;
        this.url = url;
        this.owner = account;
    }
}
