package com.example.servermaintenance.course;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Data
public class CourseSchemaInputDto {
    private List<CourseSchemaPartDto> parts;
    private List<CourseStudentPartDto> data;
    private Map<Integer, String> errors;
}
