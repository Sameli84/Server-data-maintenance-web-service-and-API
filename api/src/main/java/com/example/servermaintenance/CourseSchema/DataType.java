package com.example.servermaintenance.CourseSchema;

import com.opencsv.bean.CsvIgnore;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "data_type")
public class DataType implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "dataType")
    private Set<CourseSchemaPart> courseSchemaParts = new HashSet<>();
}
