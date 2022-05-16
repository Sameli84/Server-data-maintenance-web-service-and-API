package com.example.servermaintenance.course;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CourseUrlToCourseConverter implements Converter<String, Course> {
    private final CourseService courseService;

    @Override
    public Course convert(String source) {
        return courseService.getCourseByUrl(source).orElseThrow();
    }
}
