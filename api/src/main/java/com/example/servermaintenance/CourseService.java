package com.example.servermaintenance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CourseService {
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountService accountService;

    public Course newCourse(String name, String url, Account account) throws Exception {
        // TODO: slugify url?
        if (courseRepository.existsByUrl(url)) {
            throw new Exception("already exists");
        }

        var c = new Course(name, url, account);

        return courseRepository.save(c);
    }

    public Course newCourseContext(String name, String url) throws Exception {
        var account = accountService.getContextAccount().orElseThrow(() -> new UsernameNotFoundException("not logged in"));
        return newCourse(name, url, account);
    }

    public boolean addToCourse(String courseUrl, Account account) {
        var course = courseRepository.findCourseByUrl(courseUrl);
        if (course.isEmpty()) {
            return false;
        }
        course.get().addStudent(account);
        courseRepository.save(course.get());
        accountRepository.save(account);
        return true;
    }

    public boolean addToCourseContext(String courseUrl) {
        var account = accountService.getContextAccount();
        if (account.isEmpty()) {
            return false;
        }
        return addToCourse(courseUrl, account.get());
    }

    public Optional<Course> getCourseByUrl(String url) {
        return courseRepository.findCourseByUrl(url);
    }
}
