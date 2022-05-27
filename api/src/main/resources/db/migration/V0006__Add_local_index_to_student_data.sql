alter table course_student
    add column course_local_index bigint;


update course_student cs
set course_local_index = (select count(id)
                          from course_student
                          where course_id = cs.course_id
                            and id < cs.id)
where cs.course_local_index is null;