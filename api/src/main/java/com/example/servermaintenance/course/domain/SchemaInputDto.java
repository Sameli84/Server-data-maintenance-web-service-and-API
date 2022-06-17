package com.example.servermaintenance.course.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Data
public class SchemaInputDto {
    private List<SchemaPartDto> parts;
    private List<CourseStudentPartDto> data;
    private Map<Integer, String> errors;
}
