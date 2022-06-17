create or replace function view_insert_course_student()
    returns trigger as
$body$
begin
    update course_index
    set index = index + 1
    where course_id = new.course_id;
    return null;
end;
$body$ language plpgsql;

create or replace function view_insert_course()
    returns trigger as
$body$
begin
    insert into course_index (course_id, index)
    values (new.id, 0);
    return null;
end;
$body$ language plpgsql;
