package com.example.servermaintenance.course;

import com.example.servermaintenance.datarow.DataRow;
import com.example.servermaintenance.account.Account;
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
@Table(name = "course")
public class Course extends AbstractPersistable<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "url", unique = true)
    private String url;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "course")
    private List<DataRow> data;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private Account owner;

    @OneToMany(mappedBy = "course")
    private Set<CourseKey> courseKeys = new HashSet<>();

    @OneToMany(mappedBy = "course")
    private Set<CourseStudent> courseStudents = new HashSet<>();

    @OneToMany(mappedBy = "course")
    private Set<SchemaPart> schemaParts = new HashSet<>();

    @OneToOne(mappedBy = "course")
    @PrimaryKeyJoinColumn
    private CourseIndex courseIndex;

    public Course(String name, String url, Account account) {
        this.name = name;
        this.url = url;
        this.owner = account;
    }
}
