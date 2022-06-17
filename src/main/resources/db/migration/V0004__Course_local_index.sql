create table course_index
(
    course_id bigint unique references course (id) on delete cascade,
    index     bigint default 0
);

create function view_insert_course_student()
    returns trigger as
$body$
begin
    update course_index
    set index = index + 1
    where course_id = new.course_id;
end;
$body$ language plpgsql;

drop trigger if exists course_student_insert
    on course_student;

create trigger course_student_insert
    after insert
    on course_student
    for each row
execute function view_insert_course_student();