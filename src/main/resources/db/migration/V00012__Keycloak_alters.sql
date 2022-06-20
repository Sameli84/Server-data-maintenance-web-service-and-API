alter table account
    drop column key_cloak_id;

alter table account
    add column keycloak_uuid uuid;

create index on account (keycloak_uuid);

drop table if exists account_roles;
drop table if exists role;
drop table if exists student_course;