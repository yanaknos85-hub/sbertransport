drop table if exists telemechanic.driving_license;

create table if not exists telemechanic.driving_license
(
    id          uuid
        constraint driving_license_pkey primary key,
    series      varchar(40) not null,
    number int4             not null,
    issue_date  date        not null,
    expiry_date date        not null,
    previous_id uuid
);

comment on table telemechanic.driving_license is 'Водительское удостоверение';
comment on column telemechanic.driving_license.id is 'Идентификатор';
comment on column telemechanic.driving_license.series is 'Серия';
comment on column telemechanic.driving_license.number is 'Номер';
comment on column telemechanic.driving_license.issue_date is 'Дата выдачи';
comment on column telemechanic.driving_license.expiry_date is 'Дата окончания срока действия';
comment on column telemechanic.driving_license.previous_id is 'Идентификатор предыдущего водительского удостоверения';