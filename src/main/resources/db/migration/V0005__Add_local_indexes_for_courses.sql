insert into course_index
select c.id, (select count(*) from course_student cs where cs.course_id = c.id)
from course c;

create function view_insert_course()
    returns trigger as
$body$
begin
    insert into course_index (course_id, index)
    values (new.id, 0);
end;
$body$ language plpgsql;

drop trigger if exists course_insert
    on course;

create trigger course_insert
    after insert
    on course
    for each row
execute function view_insert_course();
