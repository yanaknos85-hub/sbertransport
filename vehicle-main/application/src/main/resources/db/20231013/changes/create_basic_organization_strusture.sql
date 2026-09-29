create table vehicle.organization
(
    id            uuid not null
        constraint organization_pkey
            primary key,
    digit_id      numeric,
    active        boolean,
    official_name varchar(255)
);

comment
on table vehicle.organization is 'Организация';

comment
on column vehicle.organization.id is 'Идентификатор записи об организации';

comment
on column vehicle.organization.digit_id is 'Уникальный идентификатор (числовой)';

comment
on column vehicle.organization.active is 'Флаг активности';

comment
on column vehicle.organization.official_name is 'Служебное название';


create table vehicle.department
(
    id                uuid         not null
        constraint department_pkey
            primary key,
    active            boolean,
    department_name   varchar(255) not null,
    human_readable_id varchar(255) not null,
    organization_id   uuid         not null
        constraint department_organization_id_fk
            references vehicle.organization,
    parent_id         uuid
        constraint department_department_id_fk
            references vehicle.department
);

comment
on table vehicle.department is 'Подразделение';

comment
on column vehicle.department.id is 'Идентификатор записи о подразделении';

comment
on column vehicle.department.active is 'Флаг активности';

comment
on column vehicle.department.department_name is 'Наименование подразделения';

comment
on column vehicle.department.human_readable_id is 'Человекочитаемый идентификатор';

comment
on column vehicle.department.organization_id is 'Идентификатор записи об организации';

comment
on column vehicle.department.parent_id is 'Идентификатор записи родителя в таблице department';

create index department_parent_id_index
    on vehicle.department (parent_id);

create table vehicle.position
(
    id              uuid         not null
        constraint position_pkey
            primary key,
    active          boolean,
    organization_id uuid
        constraint position_organization_id_fk
            references vehicle.organization,
    position_name   varchar(255) not null
);

comment
on table vehicle.position is 'Должность';

comment
on column vehicle.position.id is 'Идентификатор записи о должности';

comment
on column vehicle.position.active is 'Флаг активности';

comment
on column vehicle.position.organization_id is 'Идентификатор записи об организации';

comment
on column vehicle.position.position_name is 'Наименование позиции';


create table vehicle.employee
(
    id                uuid         not null
        constraint employee_pkey
            primary key,
    active            boolean,
    human_readable_id varchar(255) not null,
    first_name        varchar(255) not null,
    last_name         varchar(255) not null,
    patronymic        varchar(255),
    mobile_phone      varchar(255),
    personnel_number  varchar(255) not null,
    user_id           uuid         not null
        constraint uk_user_employee
            unique,
    department_id     uuid         not null
        constraint employee_department_id_fk
            references vehicle.department,
    position_id       uuid         not null
        constraint employee_position_id_fk
            references vehicle.position,
    organization_id   uuid
        constraint employee_organization_id_fk
            references vehicle.organization
);

comment
on table vehicle.employee is 'Сотрудник';

comment
on column vehicle.employee.id is 'Идентификатор записи о сотруднике';

comment
on column vehicle.employee.active is 'Флаг активности';

comment
on column vehicle.employee.human_readable_id is 'Человекочитаемый идентификатор';

comment
on column vehicle.employee.first_name is 'Имя';

comment
on column vehicle.employee.last_name is 'Фамилия';

comment
on column vehicle.employee.patronymic is 'Отчество';

comment
on column vehicle.employee.mobile_phone is 'Номер телефона';

comment
on column vehicle.employee.personnel_number is 'Табельный номер';

comment
on column vehicle.employee.user_id is 'Идентификатор записи с таблицы corporate.user';

comment
on column vehicle.employee.department_id is 'Идентификатор записи о подразделении';

comment
on column vehicle.employee.position_id is 'Идентификатор записи о должности';

comment
on column vehicle.employee.organization_id is 'Идентификатор записи об организации';

create index employee_department_id_index
    on vehicle.employee (department_id);

create index employee_organization_id_index
    on vehicle.employee (organization_id);