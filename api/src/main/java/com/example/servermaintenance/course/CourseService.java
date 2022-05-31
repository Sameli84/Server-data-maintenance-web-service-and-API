package com.example.servermaintenance.course;

import com.example.servermaintenance.account.RoleService;
import com.example.servermaintenance.account.Account;
import com.example.servermaintenance.course.domain.*;
import com.github.slugify.Slugify;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.Writer;
import java.util.*;

@Service
@AllArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final CourseKeyRepository courseKeyRepository;
    private final RoleService roleService;
    private final CourseSchemaPartRepository courseSchemaPartRepository;

    private ModelMapper modelMapper;
    private final CourseStudentService courseStudentService;
    private final SchemaPartRepository schemaPartRepository;
    private final CourseStudentPartRepository courseStudentPartRepository;

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
        return course.getCourseStudents().stream().map(CourseStudent::getAccount).toList().contains(account);
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

        courseStudentService.deleteCourseStudent(course, account);
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

//        var beanToCsv = new StatefulBeanToCsvBuilder<DataRow>(w).build();
//
//        var data = dataRowService.getCourseDataRows(course.get());
//        beanToCsv.write(data);
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

    @Transactional
    public CourseSchemaInputDto getStudentForm(Course course, Account account) {
        var schema = schemaPartRepository.findSchemaPartsByCourseOrderByOrder(course);
        var dataParts = courseStudentService.getCourseStudentParts(course, account);
        var result = new ArrayList<CourseSchemaPartDto>(schema.size());
        var data = new ArrayList<CourseStudentPartDto>(schema.size());
        for (int i = 0; i < schema.size(); i++) {
            var courseSchemaPartDto = modelMapper.map(schema.get(i), CourseSchemaPartDto.class);
            result.add(courseSchemaPartDto);
            data.add(new CourseStudentPartDto(dataParts.get(i).getData()));
        }
        return new CourseSchemaInputDto(result, data, null);
    }

    @Transactional
    public CourseDataDto getCourseData(Course course) {
        var students = courseStudentService.getCourseStudents(course);
        var rows = new ArrayList<CourseDataRowDto>();

        for (var student : students) {
            var row = new CourseDataRowDto();
            row.setIndex(student.getCourseLocalIndex());
            row.setParts(courseStudentPartRepository.findCourseStudentPartsByCourseStudentOrderBySchemaPart_Order(student));
            rows.add(row);
        }

        var headers = course.getSchemaParts()
                .stream()
                .sorted(Comparator.comparingInt(SchemaPart::getOrder))
                .map(SchemaPart::getName)
                .toList();

        return new CourseDataDto(headers, rows);
    }
}
