package com.example.servermaintenance.course;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Data
public class CourseSchemaInputDto {
    private List<CourseSchemaPartDto> parts = new ArrayList<>();
}
