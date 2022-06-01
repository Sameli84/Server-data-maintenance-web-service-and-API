package com.example.servermaintenance.course.domain;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "course_student_part")
public class CourseStudentPart implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "data")
    private String data;

    @ManyToOne
    @JoinColumn(name = "course_student_id")
    private CourseStudent courseStudent;

    @ManyToOne
    @JoinColumn(name = "schema_part_id")
    private SchemaPart schemaPart;

    public CourseStudentPart(CourseStudent courseStudent, SchemaPart schemaPart) {
        this.data = data;
        this.courseStudent = courseStudent;
        this.schemaPart = schemaPart;
    }
}
