/* migrate student_course to course_students_data */
insert into course_student_data (course_id, account_id)
select course_id, account_id
from student_course;

drop table student_course;

alter table course_student_data
    rename to course_student;

alter sequence course_student_data_id_seq
    rename to course_student_id_seq;


alter table course_schema_part
    rename to schema_part;

alter sequence course_schema_part_id_seq
    rename to schema_part_id_seq;


alter table course_data_part
    rename to student_course_data_part;

alter sequence course_data_part_id_seq
    rename to student_course_data_part_id_seq;