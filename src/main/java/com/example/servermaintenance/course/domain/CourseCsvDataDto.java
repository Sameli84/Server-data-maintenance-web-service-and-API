package com.example.servermaintenance.course.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CourseCsvDataDto {
    String[] headers;
    List<String[]> rows;
}
