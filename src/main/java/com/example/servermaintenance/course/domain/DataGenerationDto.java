package com.example.servermaintenance.course.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataGenerationDto {
    private int target;
    private String statement;
    private List<Boolean> selectedRows;
}
