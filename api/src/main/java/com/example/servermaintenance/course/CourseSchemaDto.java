package com.example.servermaintenance.course;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Data
public class CourseSchemaDto {
    @Size(min = 4, max = 64, message = "Course name must be between 4 and 64 characters long")
    private String courseName;

    @Size(max = 16, message = "Course key has to be less than 16 characters")
    private String key;
    @NotEmpty(message = "Course has to have parts")
    private List<CourseSchemaPartDto> parts = new ArrayList<>();

    public void addPart(CourseSchemaPartDto part) {
        this.parts.add(part);
    }

    public int size() {
        return this.parts.size();
    }
}
