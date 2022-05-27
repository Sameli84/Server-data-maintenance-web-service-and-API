alter table student_course_data_part
    rename column data_schema_part_id to schema_part_id;
alter table student_course_data_part
    rename column course_data_id to course_student_id;

alter table student_course_data_part
    rename to course_student_part;

alter sequence student_course_data_part_id_seq
    rename to course_student_part_id_seq;

