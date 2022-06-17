alter table account
    drop column key_cloak_id;

alter table account
    add column keycloak_uuid uuid;

alter table account
    drop column name;

create index on account (keycloak_uuid);

drop table account_roles;
drop table role;
drop table if exists student_course;