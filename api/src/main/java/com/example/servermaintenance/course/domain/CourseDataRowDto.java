package com.example.servermaintenance.course.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CourseDataRowDto {
    long index;
    List<CourseStudentPart> parts = new ArrayList<>();
}
