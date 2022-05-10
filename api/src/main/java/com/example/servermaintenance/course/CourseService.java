package com.example.servermaintenance.course;

import com.example.servermaintenance.datarow.DataRow;
import com.example.servermaintenance.datarow.DataRowRepository;
import com.example.servermaintenance.account.Account;
import com.example.servermaintenance.account.AccountRepository;
import com.example.servermaintenance.account.AccountService;
import com.example.servermaintenance.datarow.DataRowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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
    private DataRowService dataRowService;

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

    @Transactional
    public boolean joinToCourse(String courseUrl, Account account) {
        var course = courseRepository.findCourseByUrl(courseUrl);
        if (course.isEmpty()) {
            return false;
        }
        if (course.get().getStudents().contains(account)) {
            return false;
        }
        course.get().addStudent(account);
        courseRepository.save(course.get());
        accountRepository.save(account);
        return true;
    }

    @Transactional
    public boolean kickFromCourse(String courseUrl, Account account) {
        var course = courseRepository.findCourseByUrl(courseUrl);
        System.out.println(account);
        System.out.println(course.get());
        if (course.isEmpty()) {
            return false;
        }
        if (!course.get().getStudents().contains(account)) {
            return false;
        }
        course.get().removeStudent(account);
        account.getCourses().remove(course.get());
        courseRepository.save(course.get());
        accountRepository.save(account);

        dataRowService.removeDataRow(course.get(), account);

        return true;
    }

    public boolean joinToCourseContext(String courseUrl) {
        var account = accountService.getContextAccount();
        if (account.isEmpty()) {
            return false;
        }
        return joinToCourse(courseUrl, account.get());
    }

    public Optional<Course> getCourseByUrl(String url) {
        return courseRepository.findCourseByUrl(url);
    }

    public void updateStudentsData(DataRow data) {
        dataRowRepository.save(data);
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
