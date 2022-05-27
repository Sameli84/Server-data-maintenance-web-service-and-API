package com.example.servermaintenance.course;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CourseDataRowDto {
    long id;
    List<CourseStudentPartDto> parts = new ArrayList<>();
}
