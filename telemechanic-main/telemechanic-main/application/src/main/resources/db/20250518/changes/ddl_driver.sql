create table if not exists telemechanic.driver
(
    id                  uuid            not null
        constraint driver_pkey                  primary key,
    employee_id         uuid            not null,
    constraint driver_employee_id_fk foreign key (employee_id)
            references telemechanic.employee (id),
    driving_license_id  uuid            not null,
    constraint driver_driving_license_id_fk foreign key (driving_license_id)
            references telemechanic.driving_license (id),
    tin                 varchar(12)     not null,
    snils               varchar(16)
);

comment on table telemechanic.driver is 'Водитель';
comment on column telemechanic.driver.id is 'Идентификатор водителя';
comment on column telemechanic.driver.employee_id is 'Идентификатор сотрудника';
comment on column telemechanic.driver.driving_license_id is 'Идентификатор водительского удостоверения';
comment on column telemechanic.driver.tin is 'ИНН';
comment on column telemechanic.driver.snils is 'СНИЛС';

create index if not exists driver_employee_id_index
    on telemechanic.driver (employee_id);

create index if not exists driver_driving_license_id_index
    on telemechanic.driver (driving_license_id);
