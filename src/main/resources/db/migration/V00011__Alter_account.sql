alter table account
    DROP column password;
alter table account
    ADD column key_cloak_id text;