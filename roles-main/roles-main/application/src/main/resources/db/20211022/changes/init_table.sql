create schema roles;
comment on schema roles is 'Роли';

create table roles.role
(
    code        varchar(255)          not null
        constraint role_pkey
            primary key,
    name        varchar(255)          not null
        constraint role_name_key
            unique,
    description text,
    "default"   boolean default false not null
);

comment on table roles.role is 'Список ролей';