package com.example.servermaintenance.course;

import com.example.servermaintenance.WebConfig;
import com.example.servermaintenance.account.RoleService;
import com.example.servermaintenance.datarow.DataRow;
import com.example.servermaintenance.account.Account;
import com.example.servermaintenance.account.AccountRepository;
import com.example.servermaintenance.datarow.DataRowService;
import com.github.slugify.Slugify;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
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
    private final CourseSchemaPartRepository courseSchemaPartRepository;
    private ModelMapper modelMapper;
    private final CourseStudentService courseStudentService;


    @Transactional
    public Course createCourse(CourseSchemaDto courseSchemaDto, Account account) {
        var slug = String.format("%s-%d", new Slugify().slugify(courseSchemaDto.getCourseName()), courseRepository.count() + 1);
        var course = courseRepository.save(new Course(courseSchemaDto.getCourseName(), slug, account));
        if (!courseSchemaDto.getKey().isEmpty()) {
            courseKeyRepository.save(new CourseKey(courseSchemaDto.getKey(), course));
        }

        for (int i = 0; i < courseSchemaDto.getParts().size(); i++) {
            var p = courseSchemaDto.getParts().get(i);

            // TODO: Currently saves nulls, prevent!!
            SchemaPart part = modelMapper.map(p, SchemaPart.class);
            part.setCourse(course);
            part.setOrder(i);
            courseSchemaPartRepository.save(part);
        }
        return course;
    }

    public boolean isStudentOnCourse(Course course, Account account) {
        return course.getCourseStudentData().stream().map(CourseStudent::getAccount).toList().contains(account);
    }

    @Transactional
    public boolean joinToCourse(Course course, Account account, String key) {
        if (isStudentOnCourse(course, account)) {
            return false;
        }

        var courseKeys = course.getCourseKeys();
        if (courseKeys.size() > 0) {
            if (!courseKeys.stream().map(CourseKey::getKey).toList().contains(key)) {
                return false;
            }
        }

        return courseStudentService.generate(course, account, course.getCourseIndex().getIndex()) != null;
    }

    @Transactional
    public boolean kickFromCourse(Course course, Account account) {
        if (!isStudentOnCourse(course, account)) {
            return false;
        }

        course.getCourseStudentData().removeIf(courseStudentData -> courseStudentData.getAccount().equals(account));
        account.getCourses().remove(course);
        courseRepository.save(course);
        accountRepository.save(account);

        dataRowService.removeDataRow(course, account);

        return true;
    }

    public Optional<Course> getCourseByUrl(String url) {
        return courseRepository.findCourseByUrl(url);
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
