package com.example.servermaintenance.course.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
public class CourseNameDto {
    @Size(min = 4, max = 64, message = "Course name must be between 4 and 64 characters long")
    private String courseName;
}
