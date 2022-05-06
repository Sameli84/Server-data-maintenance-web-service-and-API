package com.example.servermaintenance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private DataRowRepository dataRowRepository;

    @Autowired
    private AccountService accountService;

    public Optional<Course> newCourse(String name, String url, Account account) {
        // TODO: slugify url?
        if (courseRepository.existsByUrl(url)) {
            return Optional.empty();
        }

        return Optional.of(courseRepository.save(new Course(name, url, account)));
    }

    public Optional<Course> newCourseContext(String name, String url) {
        var account = accountService.getContextAccount();
        if (account.isPresent()) {
            return newCourse(name, url, account.get());
        } else {
            return Optional.empty();
        }
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

    public void updateStudentsData(Course course, Account account, String studentAlias, String cscUsername, int uid, String dnsName, String selfMadeDnsName, String name, String vpsUsername, String poutaDns, String ipAddress) {
        dataRowRepository.save(new DataRow(studentAlias, cscUsername, uid, dnsName, selfMadeDnsName, name, vpsUsername, poutaDns, ipAddress, account, course));
    }

    public Boolean checkIfStudentOnCourse(Course course, Account account) {
        return course.getStudents().contains(account);
    }

    public List<Course> getCourses() {
        return courseRepository.findAll();
    }

    public Course getCourseById(Long id) {
        return courseRepository.getById(id);
    }
}
