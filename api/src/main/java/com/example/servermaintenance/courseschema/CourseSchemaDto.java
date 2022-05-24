package com.example.servermaintenance.courseschema;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CourseSchemaDto {
    private List<CourseSchemaPartDto> parts = new ArrayList<>();

    public void addPart(CourseSchemaPartDto part) {
        this.parts.add(part);
    }

    public int size() {
        return this.parts.size();
    }
}
