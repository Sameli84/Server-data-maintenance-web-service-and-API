package com.example.servermaintenance.course.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CourseDataInputDto {
    private CourseDataDto courseDataDto;
    private boolean edit;

    public CourseDataInputDto(CourseDataDto courseDataDto) {
        this.courseDataDto = courseDataDto;
    }
}
