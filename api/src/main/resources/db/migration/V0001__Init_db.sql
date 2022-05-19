create sequence hibernate_sequence;

create table account
(
    id         bigserial primary key,
    email      text unique not null,
    first_name text        not null,
    last_name  text        not null,
    password   text        not null
);

create table role
(
    id   bigserial primary key,
    name varchar(24) not null
);

insert into role (name)
values ('ROLE_ADMIN'),
       ('ROLE_TEACHER'),
       ('ROLE_STUDENT');

create table account_roles
(
    account_id bigint references account (id) on delete cascade,
    role_id    bigint references role (id) on delete cascade,
    constraint account_roles_pk
        primary key (account_id, role_id)
);

create table course
(
    id       bigserial primary key,
    name     text        not null,
    url      text unique not null,
    owner_id bigint references account (id) on delete cascade
);

create table course_key
(
    id        bigserial primary key,
    key       text,
    course_id bigint references course (id) on delete cascade
);

create table student_course
(
    course_id  bigint references course (id) on delete cascade,
    account_id bigint references account (id) on delete cascade,
    constraint student_course_pk
        primary key (course_id, account_id)
);

create table datarow
(
    id            bigserial primary key,
    account_id    bigint references account (id) on delete cascade,
    course_id     bigint references course (id) on delete cascade,
    project       text,
    student_alias text,
    csc_username  text,
    userid        int,
    vps_username  text,
    ip_address    text,
    pouta_dns     text,
    self_made_dns text
);
