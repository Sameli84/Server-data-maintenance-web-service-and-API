package com.example.servermaintenance.course;

import com.example.servermaintenance.account.RoleService;
import com.example.servermaintenance.datarow.DataRow;
import com.example.servermaintenance.account.Account;
import com.example.servermaintenance.account.AccountRepository;
import com.example.servermaintenance.datarow.DataRowService;
import com.github.slugify.Slugify;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.Writer;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final AccountRepository accountRepository;
    private final DataRowService dataRowService;
    private final CourseKeyRepository courseKeyRepository;
    private final RoleService roleService;

    @Transactional
    public Course newCourse(CourseCreationDTO courseCreationDTO, Account account) {
        var slug = String.format("%s-%d", new Slugify().slugify(courseCreationDTO.getCourseName()), courseRepository.count() + 1);
        var course = courseRepository.save(new Course(courseCreationDTO.getCourseName(), slug, account));
        if (!courseCreationDTO.getKey().isEmpty()) {
            courseKeyRepository.save(new CourseKey(courseCreationDTO.getKey(), course));
        }
        return course;
    }

    @Transactional
    public boolean joinToCourse(Course course, Account account, String key) {
        if (course.getStudents().contains(account)) {
            return false;
        }

        var courseKeys = course.getCourseKeys();
        if (courseKeys.size() > 0) {
            if (!courseKeys.stream().map(CourseKey::getKey).toList().contains(key)) {
                return false;
            }
        }

        course.addStudent(account);
        courseRepository.save(course);
        accountRepository.save(account);
        dataRowService.generateData(course, account);
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

    public Optional<Course> getCourseByUrl(String url) {
        return courseRepository.findCourseByUrl(url);
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

        var data = dataRowService.getCourseDataRows(course.get());
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

    @Transactional
    public boolean deleteCourse(Course course, Account account) {
        if (Objects.equals(course.getOwner().getId(), account.getId()) || roleService.isAdmin(account)) {
            courseRepository.delete(course);
            return true;
        }
        return false;
    }

    public List<Course> getCoursesByTeacher(Account account) {
        return courseRepository.findAllByOwner(account);
    }

    public boolean hasCourseKey(Course course) {
        return courseKeyRepository.existsCourseKeyByCourse(course);
    }
}
