package com.example.servermaintenance.course.domain;


import com.example.servermaintenance.account.Account;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "course_student")
public class CourseStudent implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "course_local_index")
    private long courseLocalIndex;

    @OneToMany(mappedBy = "courseStudent")
    private Set<CourseStudentPart> courseStudentParts = new HashSet<>();

    public CourseStudent(Account account, Course course, long courseLocalIndex) {
        this.account = account;
        this.course = course;
        this.courseLocalIndex = courseLocalIndex;
    }

    public void addCourseStudentPart(CourseStudentPart courseStudentPart) {
        this.courseStudentParts.add(courseStudentPart);
    }
}
