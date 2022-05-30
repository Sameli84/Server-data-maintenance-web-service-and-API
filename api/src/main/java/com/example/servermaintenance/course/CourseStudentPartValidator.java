package com.example.servermaintenance.course;

import lombok.AllArgsConstructor;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class CourseStudentPartValidator implements Validator {
    private final String name;
    private final String regex;
    private final String message;

    @Override
    public boolean supports(Class clazz) {
        return CourseStudentPartDto.class.equals(clazz);
    }

    public CourseStudentPartValidator(String name, String regex, String message) {
        this.name = name;
        this.regex = regex;
        this.message = message;
    }

    @Override
    public void validate(Object target, Errors errors) {
        var part = (CourseStudentPartDto)target;
        if (!part.getData().matches(regex)) {
            errors.reject(name, message);
        }
    }
}
