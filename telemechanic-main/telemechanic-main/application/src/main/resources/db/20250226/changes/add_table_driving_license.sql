create table telemechanic.driving_license (
    id uuid primary key not null,
    driver_id uuid not null,
    series varchar(40) not null,
    number int4 not null,
    issue_date date not null,
    expiry_date date not null,
    previous_id uuid,
    constraint driving_license_driver_id_fkey foreign key (driver_id) references telemechanic.employee (id)
);

comment on table telemechanic.driving_license is 'Водительское удостоверение';

comment on column telemechanic.driving_license.id is 'Идентификатор';
comment on column telemechanic.driving_license.driver_id is 'Идентификатор водителя';
comment on column telemechanic.driving_license.series is 'Серия';
comment on column telemechanic.driving_license.number is 'Номер';
comment on column telemechanic.driving_license.issue_date is 'Дата выдачи';
comment on column telemechanic.driving_license.expiry_date is 'Дата истечения';
comment on column telemechanic.driving_license.previous_id is 'Идентификатор предыдущего водительского удостоверения';