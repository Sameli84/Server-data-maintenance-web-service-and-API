package com.example.servermaintenance.course.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseStudentPartDto {
    private String data;
    private CourseStudentPart _courseStudentPart;

    public CourseStudentPartDto(String data) {
        this.data = data;
    }
}
