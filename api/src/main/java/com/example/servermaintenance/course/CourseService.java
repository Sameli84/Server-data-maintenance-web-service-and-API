package com.example.servermaintenance.course;

import com.example.servermaintenance.datarow.DataRow;
import com.example.servermaintenance.datarow.DataRowRepository;
import com.example.servermaintenance.account.Account;
import com.example.servermaintenance.account.AccountRepository;
import com.example.servermaintenance.account.AccountService;
import com.example.servermaintenance.datarow.DataRowService;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.Writer;
import java.util.List;
import java.util.Objects;
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

    @Autowired
    private CourseKeyRepository courseKeyRepository;

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
    public boolean joinToCourse(String courseUrl, Account account, String key) {
        var course = courseRepository.findCourseByUrl(courseUrl);
        if (course.isEmpty()) {
            return false;
        }
        if (course.get().getStudents().contains(account)) {
            return false;
        }

        var courseKeys = course.get().getCourseKeys();
        if (courseKeys.size() > 0) {
            if (!courseKeys.stream().map(CourseKey::getKey).toList().contains(key)) {
                return false;
            }
        }

        course.get().addStudent(account);
        courseRepository.save(course.get());
        accountRepository.save(account);
        return true;
    }

    @Transactional
    public boolean kickFromCourse(Course course, Account account) {
        if (!course.getStudents().contains(account)) {
            return false;
        }
        course.removeStudent(account);
        account.getCourses().remove(course);
        courseRepository.save(course);
        accountRepository.save(account);

        dataRowService.removeDataRow(course, account);

        return true;
    }

    public boolean joinToCourseContext(String courseUrl, String key) {
        var account = accountService.getContextAccount();
        if (account.isEmpty()) {
            return false;
        }
        return joinToCourse(courseUrl, account.get(), key);
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

    public void writeReportContext(String courseUrl, Writer w) throws Exception {
        var course = getCourseByUrl(courseUrl);
        if (course.isEmpty()) {
            throw new Exception("course not found");
        }

        var beanToCsv = new StatefulBeanToCsvBuilder<DataRow>(w).build();

        var data = dataRowService.getCourseData(course.get());
        beanToCsv.write(data);
    }

    @Transactional
    public boolean addKey(Course course, String key) {
        if (courseKeyRepository.existsCourseKeyByKey(key)) {
            return false;
        }
        courseKeyRepository.save(new CourseKey(key, course));
        return true;
    }

    @Transactional
    public boolean deleteKey(Course course, long keyId) {
        var courseKey = courseKeyRepository.findById(keyId);
        if (courseKey.isEmpty()) {
            return false;
        }

        if (!Objects.equals(courseKey.get().getCourse().getId(), course.getId())) {
            return false;
        }

        courseKeyRepository.deleteById(keyId);
        return true;
    }
}
