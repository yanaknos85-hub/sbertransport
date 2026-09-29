create table if not exists telemechanic.department_time_zone
(
    id              uuid        primary key,
    department_id   uuid        not null,
    time_zone       varchar(9)  not null,

    constraint fk_department_time_zone_department_id
        foreign key (department_id) references telemechanic.department (id)
);

create index if not exists idx_department_time_zone_department_id on telemechanic.department_time_zone (department_id);

comment on table telemechanic.department_time_zone is 'Часовой пояс подразделения';

comment on column telemechanic.department_time_zone.id              is 'Идентификатор';
comment on column telemechanic.department_time_zone.department_id   is 'Идентификатор подразделения';
comment on column telemechanic.department_time_zone.time_zone       is 'Часовой пояс в формате UTC+03:00';
