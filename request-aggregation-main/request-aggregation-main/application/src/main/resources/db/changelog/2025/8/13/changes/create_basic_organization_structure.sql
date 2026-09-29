create table request_aggregation.organization
(
    id            uuid not null
        constraint organization_pkey
            primary key,
    digit_id      numeric,
    active        boolean,
    official_name varchar(255)
);

comment
on table request_aggregation.organization is 'Организация';

comment
on column request_aggregation.organization.id is 'Идентификатор записи об организации';

comment
on column request_aggregation.organization.digit_id is 'Уникальный идентификатор (числовой)';

comment
on column request_aggregation.organization.active is 'Флаг активности';

comment
on column request_aggregation.organization.official_name is 'Служебное название';


create table request_aggregation.department
(
    id                uuid         not null
        constraint department_pkey
            primary key,
    active            boolean,
    department_name   varchar(255) not null,
    human_readable_id varchar(255) not null,
    organization_id   uuid         not null
        constraint department_organization_id_fk
            references request_aggregation.organization,
    parent_id         uuid
        constraint department_department_id_fk
            references request_aggregation.department
);

comment
on table request_aggregation.department is 'Подразделение';

comment
on column request_aggregation.department.id is 'Идентификатор записи о подразделении';

comment
on column request_aggregation.department.active is 'Флаг активности';

comment
on column request_aggregation.department.department_name is 'Наименование подразделения';

comment
on column request_aggregation.department.human_readable_id is 'Человекочитаемый идентификатор';

comment
on column request_aggregation.department.organization_id is 'Идентификатор записи об организации';

comment
on column request_aggregation.department.parent_id is 'Идентификатор записи родителя в таблице department';

create index department_parent_id_index
    on request_aggregation.department (parent_id);

create table request_aggregation.position
(
    id              uuid         not null
        constraint position_pkey
            primary key,
    active          boolean,
    organization_id uuid
        constraint position_organization_id_fk
            references request_aggregation.organization,
    position_name   varchar(255) not null
);

comment
on table request_aggregation.position is 'Должность';

comment
on column request_aggregation.position.id is 'Идентификатор записи о должности';

comment
on column request_aggregation.position.active is 'Флаг активности';

comment
on column request_aggregation.position.organization_id is 'Идентификатор записи об организации';

comment
on column request_aggregation.position.position_name is 'Наименование позиции';


create table request_aggregation.employee
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
            references request_aggregation.department,
    position_id       uuid         not null
        constraint employee_position_id_fk
            references request_aggregation.position,
    organization_id   uuid
        constraint employee_organization_id_fk
            references request_aggregation.organization,
    cost_center                 varchar(256),
    itinerant_type              varchar(256)
);

comment
on table request_aggregation.employee is 'Сотрудник';

comment
on column request_aggregation.employee.id is 'Идентификатор записи о сотруднике';

comment
on column request_aggregation.employee.active is 'Флаг активности';

comment
on column request_aggregation.employee.human_readable_id is 'Человекочитаемый идентификатор';

comment
on column request_aggregation.employee.first_name is 'Имя';

comment
on column request_aggregation.employee.last_name is 'Фамилия';

comment
on column request_aggregation.employee.patronymic is 'Отчество';

comment
on column request_aggregation.employee.mobile_phone is 'Номер телефона';

comment
on column request_aggregation.employee.personnel_number is 'Табельный номер';

comment
on column request_aggregation.employee.user_id is 'Идентификатор записи с таблицы corporate.user';

comment
on column request_aggregation.employee.department_id is 'Идентификатор записи о подразделении';

comment
on column request_aggregation.employee.position_id is 'Идентификатор записи о должности';

comment
on column request_aggregation.employee.organization_id is 'Идентификатор записи об организации';

comment
on column request_aggregation.employee.cost_center is 'Место возникновения затрат';

comment
on column request_aggregation.employee.itinerant_type is 'Тип разъездного характера сотрудника';

create index employee_department_id_index
    on request_aggregation.employee (department_id);

create index employee_organization_id_index
    on request_aggregation.employee (organization_id);