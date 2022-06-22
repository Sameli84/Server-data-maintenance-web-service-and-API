package com.example.servermaintenance.course;

import com.example.servermaintenance.account.Account;
import com.example.servermaintenance.account.AccountService;
import com.example.servermaintenance.course.domain.*;
import com.github.slugify.Slugify;
import com.opencsv.CSVWriter;
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
    private final CourseSchemaPartRepository courseSchemaPartRepository;

    private ModelMapper modelMapper;
    private final CourseStudentService courseStudentService;
    private final SchemaPartRepository schemaPartRepository;
    private final CourseStudentPartRepository courseStudentPartRepository;

    private final CourseStudentRepository courseStudentRepository;
    private final AccountService accountService;

    @Transactional
    public Course createCourse(CourseCreationDto creationDto, Account account) {
        var slug = String.format("%s-%d", new Slugify().slugify(creationDto.getCourseName()), courseRepository.count() + 1);
        var course = courseRepository.save(new Course(creationDto.getCourseName(), slug, account));
        if (!creationDto.getKey().isEmpty()) {
            courseKeyRepository.save(new CourseKey(creationDto.getKey(), course));
        }
        return course;
    }

    @Transactional
    public boolean keyIsUnique(String key) {
        return !courseKeyRepository.existsCourseKeyByKey(key);
    }

    @Transactional
    public void saveCourseSchema(Course course, SchemaDto schemaDto) {
        var parts = schemaDto.getParts();
        var newSchemaParts = new HashSet<SchemaPart>(parts.size());

        for (int i = 0; i < parts.size(); i++) {
            var spd = parts.get(i);

            boolean isNewPart = spd.get_schemaPartEntity() == null;
            SchemaPart part = isNewPart ? new SchemaPart() : spd.get_schemaPartEntity();
            modelMapper.map(spd, part);

            part.setCourse(course);
            part.setOrder(i);
            part = courseSchemaPartRepository.save(part);
            newSchemaParts.add(part);

            if (isNewPart) {
                courseStudentService.generateNewPartStudentData(course, part);
            }
        }
        course.setSchemaParts(newSchemaParts);

        courseSchemaPartRepository.deleteAll(schemaDto.getRemovedEntities());
        courseRepository.save(course);
    }

    public boolean isStudentOnCourse(Course course, Account account) {
        return courseStudentRepository.findFirstByCourseAndAccount(course, account) != null;
    }

    @Transactional
    public boolean joinToCourse(Course course, Account account, String key) {
        if (isStudentOnCourse(course, account)) {
            return false;
        }
        if (course.getOwner().equals(account) || account.getRoles().contains("ROLE_ADMIN")) {
            return courseStudentService.generate(course, account, course.getCourseIndex().getIndex()) != null;
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

    // Generates required parts when students joins a course
    @Transactional
    public CourseCsvDataDto getCourseCsvData(Course course) {
        var headers = this.courseRepository.findCourseSchemaPartNames(course);
        var rows = new ArrayList<String[]>();
        var students = courseRepository.findAllCourseStudentsFetchData(course);
        var row = new ArrayList<String>();

        for (var student : students) {
            row.clear();
            row.addAll(student.getCourseStudentParts()
                    .stream()
                    .sorted(Comparator.comparingInt(a -> a.getSchemaPart().getOrder()))
                    .map(CourseStudentPart::getData)
                    .toList());
            rows.add(row.toArray(new String[0]));
        }

        return new CourseCsvDataDto(headers, rows);
    }

    public void writeReportContext(String courseUrl, Writer w) throws Exception {
        var course = getCourseByUrl(courseUrl);
        if (course.isEmpty()) {
            throw new Exception("course not found");
        }
        var csvData = getCourseCsvData(course.get());

        CSVWriter writer = new CSVWriter(w);

        writer.writeNext(csvData.getHeaders());
        writer.writeAll(csvData.getRows());

        writer.close();
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
        if (isOwnerOrAdmin(course, account)) {
            courseRepository.delete(course);
            return true;
        }
        return false;
    }

    public List<Course> getCoursesByAccount(Account account) {
        var courses = courseRepository.findAllStudentOn(account);
        courses.addAll(courseRepository.findAllByOwner(account));
        return courses.stream().toList();
    }

    public boolean hasCourseKey(Course course) {
        return courseKeyRepository.existsCourseKeyByCourse(course);
    }

    // Get students data for given course for data input view
    @Transactional
    public SchemaInputDto getStudentForm(Course course, Account account) {
        // Get schema parts
        var schema = schemaPartRepository.findSchemaPartsOrdered(course);
        // Get data
        var dataParts = courseStudentService.getCourseStudentParts(course, account);
        var result = new ArrayList<SchemaPartDto>(schema.size());
        var data = new ArrayList<CourseStudentPartDto>(schema.size());
        // Combine course schema parts with respective student data
        for (int i = 0; i < schema.size(); i++) {
            var schemaPartDto = modelMapper.map(schema.get(i), SchemaPartDto.class);
            result.add(schemaPartDto);
            data.add(new CourseStudentPartDto(dataParts.get(i).getData()));
        }
        return new SchemaInputDto(result, data, null);
    }

    // Form course data table with student data as rows
    @Transactional
    public CourseDataDto getCourseData(Course course) {
        var students = courseRepository.findAllCourseStudentsFetchData(course);

        var rows = new ArrayList<CourseDataRowDto>();

        // Add a row for each students data
        for (var student : students) {
            var row = new CourseDataRowDto();
            row.setIndex(student.getCourseLocalIndex());
            row.setParts(student.getCourseStudentParts()
                    .stream()
                    .sorted(Comparator.comparingInt((CourseStudentPart a) -> a.getSchemaPart().getOrder()))
                    .map(p -> new CourseStudentPartDto(p.getData(), p))
                    .toList());
            rows.add(row);
        }

        // Add headers using course schema parts names
        var headers = course.getSchemaParts()
                .stream()
                .sorted(Comparator.comparingInt(SchemaPart::getOrder))
                .map(SchemaPart::getName)
                .toList();

        return new CourseDataDto(headers, rows);
    }

    // Direct course data modification
    @Transactional
    public void saveCourseData(CourseDataDto courseDataDto, Course course) {
        var students = this.courseStudentService.getCourseStudents(course);
        if (students.isEmpty()) {
            return;
        }

        var parts = courseDataDto.getRows()
                .stream()
                .flatMap(a -> a.getParts().stream()
                        // Get parts that have been modified
                        .filter(c -> !c.getData().equals(c.get_courseStudentPart().getData()))
                        // Update data for modified parts
                        .map(b -> {
                            b.get_courseStudentPart().setData(b.getData());
                            return b.get_courseStudentPart();
                        }))
                .filter(p -> students.contains(p.getCourseStudent()))
                .toList();

        courseStudentPartRepository.saveAll(parts);
    }

    public boolean isOwnerOrAdmin(Course course, Account account) {
        return Objects.equals(account.getId(), course.getOwner().getId()) || accountService.isAdmin(account);
    }
}
