package com.example.servermaintenance.course;

import com.example.servermaintenance.course.domain.Course;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.function.Supplier;

@NoArgsConstructor
public class CourseSessionMap<T> {
    private final HashMap<Long, T> map = new HashMap<>();

    T add(Course course, T val) {
        this.map.put(course.getId(), val);
        return val;
    }

    boolean contains(Course course) {
        return this.map.containsKey(course.getId());
    }

    T get(Course course) {
        return this.map.get(course.getId());
    }

    T create(Course course, Supplier<T> factory) {
        if (!this.map.containsKey(course.getId())) {
            this.map.put(course.getId(), factory.get());
        }
        return this.map.get(course.getId());
    }

    void remove(Course course) {
        this.map.remove(course.getId());
    }
}
