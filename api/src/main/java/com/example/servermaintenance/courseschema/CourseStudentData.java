package com.example.servermaintenance.courseschema;


import com.example.servermaintenance.account.Account;
import com.example.servermaintenance.course.Course;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "course_student_data")
public class CourseStudentData implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @OneToMany(mappedBy = "courseStudentData")
    private Set<CourseDataPart> courseDataParts = new HashSet<>();
}
