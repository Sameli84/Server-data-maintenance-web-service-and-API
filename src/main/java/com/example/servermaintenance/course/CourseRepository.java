package com.example.servermaintenance.course;

import com.example.servermaintenance.account.Account;
import com.example.servermaintenance.course.domain.Course;
import com.example.servermaintenance.course.domain.CourseStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    @Query("select distinct c from Course c join fetch c.owner join fetch c.courseIndex where c.url = :url")
    Optional<Course> findCourseByUrl(String url);
    @Query("select c from Course c join fetch c.courseIndex where c.owner = :account")
    List<Course> findAllByOwner(Account account);
    @Query("select cs.course from CourseStudent cs join fetch cs.course.courseIndex where cs.account = :account")
    Set<Course> findAllStudentOn(Account account);
    @Query("select distinct c.account from CourseStudent c join c.account a where c.course = :course")
    List<Account> findAllStudents(Course course);
    @Query("select distinct c from CourseStudent c join fetch c.courseStudentParts p join fetch p.schemaPart where c.course = :course order by c.courseLocalIndex")
    List<CourseStudent> findAllCourseStudentsFetchData(Course course);
    @Query("select s.name from SchemaPart s left join s.course where s.course = :course order by s.order")
    String[] findCourseSchemaPartNames(Course course);
}
