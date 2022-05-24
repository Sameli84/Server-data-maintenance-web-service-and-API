package com.example.servermaintenance.CourseSchema;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CourseSchema {
    private List<CourseSchemaPart> parts = new ArrayList<>();

    public void addPart(CourseSchemaPart part) {
        this.parts.add(part);
    }

    public int size() {
        return this.parts.size();
    }
}
