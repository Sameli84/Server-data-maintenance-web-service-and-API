package com.example.servermaintenance.course.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CourseDataDto {
    List<String> headers;
    List<CourseDataRowDto> rows;
}
