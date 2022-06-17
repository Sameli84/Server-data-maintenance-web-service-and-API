package com.example.servermaintenance.course.domain;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class CourseCreationDto {
    @Size(min = 4, max = 64, message = "Course name must be between 4 and 64 characters long")
    private String courseName;

    @Size(max = 16, message = "Course key has to be less than 16 characters")
    private String key;
}
