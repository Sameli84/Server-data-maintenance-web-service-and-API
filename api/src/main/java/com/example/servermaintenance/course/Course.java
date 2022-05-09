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
    private Account account;

    @ManyToMany
    @JoinTable(
            name = "student_course",
            joinColumns = { @JoinColumn(name = "course_id") },
            inverseJoinColumns = { @JoinColumn(name = "account_id") }
    )
    private Set<Account> students = new HashSet<>();

    public Course(String name, String url, Account account) {
        this.name = name;
        this.url = url;
        this.account = account;
    }

    public void addStudent(Account a) {
        this.students.add(a);
        a.getStudentCourses().add(this);
    }

    public void removeStudent(Account a) {
        this.students.remove(a);
        a.getStudentCourses().remove(this);
    }
}
