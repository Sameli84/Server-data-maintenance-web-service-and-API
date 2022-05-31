package com.example.servermaintenance.course;

import com.example.servermaintenance.course.domain.Course;
import lombok.AllArgsConstructor;
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
