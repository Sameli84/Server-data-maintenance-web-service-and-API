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
@Table(name = "course_index")
public class CourseIndex extends AbstractPersistable<Long> {
    @Id
    @Column(name = "course_id")
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "course_id")
    private Course course;

    private long index;
}
