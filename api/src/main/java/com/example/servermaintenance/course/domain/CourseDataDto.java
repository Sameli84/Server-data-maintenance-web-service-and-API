package com.example.servermaintenance.course.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CourseDataDto {
    List<String> headers = new ArrayList<>();
    List<CourseDataRowDto> rows = new ArrayList<>();
}
