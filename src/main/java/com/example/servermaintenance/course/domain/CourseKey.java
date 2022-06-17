package com.example.servermaintenance.course.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "course_key")
public class CourseKey extends AbstractPersistable<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    private String key;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    public CourseKey(String key, Course course) {
        this.key = key;
        this.course = course;
    }
}
