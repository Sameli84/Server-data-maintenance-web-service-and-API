package com.example.servermaintenance.CourseSchema;

import com.example.servermaintenance.datarow.DataRow;
import com.opencsv.bean.CsvIgnore;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "course_data_part")
public class CourseDataPart implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "data")
    private String data;

    @ManyToOne
    @JoinColumn(name = "course_data_id")
    private CourseStudentData courseStudentData;

    @ManyToOne
    @JoinColumn(name = "data_schema_part_id")
    private CourseSchemaPart courseSchemaPart;
}
