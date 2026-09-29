create table telemechanic.vehicle
(
    id           uuid         not null
        constraint vehicle_pkey
            primary key,
    state_number varchar(50)  not null
        constraint state_number_vehicle
            unique,
    brand        varchar(255) not null,
    model        varchar(255) not null
);

comment
    on table telemechanic.vehicle is 'Транспортное средство';

comment
    on column telemechanic.vehicle.id is 'Идентификатор записи о транспортном средстве';

comment
    on column telemechanic.vehicle.state_number is 'Автомобильный номер';

comment
    on column telemechanic.vehicle.brand is 'Марка';

comment
    on column telemechanic.vehicle.model is 'Модель';

create table telemechanic.organization
(
    id            uuid not null
        constraint organization_pkey
            primary key,
    digit_id      numeric,
    active        boolean,
    official_name varchar(255)
);

comment
    on table telemechanic.organization is 'Организация';

comment
    on column telemechanic.organization.id is 'Идентификатор записи об организации';

comment
    on column telemechanic.organization.digit_id is 'Уникальный идентификатор (числовой)';

comment
    on column telemechanic.organization.active is 'Флаг активности';

comment
    on column telemechanic.organization.official_name is 'Служебное название';



create table telemechanic.department
(
    id                uuid         not null
        constraint department_pkey
            primary key,
    active            boolean,
    department_name   varchar(255) not null,
    human_readable_id varchar(255) not null,
    easup_id          varchar(255),
    organization_id   uuid         not null
        constraint department_organization_id_fk
            references telemechanic.organization,
    parent_id         uuid
        constraint department_department_id_fk
            references telemechanic.department
);

comment
    on table telemechanic.department is 'Подразделение';

comment
    on column telemechanic.department.id is 'Идентификатор записи о подразделении';

comment
    on column telemechanic.department.active is 'Флаг активности';

comment
    on column telemechanic.department.department_name is 'Наименование подразделения';

comment
    on column telemechanic.department.human_readable_id is 'Человекочитаемый идентификатор';

comment
    on column telemechanic.department.organization_id is 'Идентификатор записи об организации';

comment
    on column telemechanic.department.parent_id is 'Идентификатор записи родителя в таблице department';

create table telemechanic.position
(
    id              uuid         not null
        constraint position_pkey
            primary key,
    active          boolean,
    organization_id uuid
        constraint position_organization_id_fk
            references telemechanic.organization,
    position_name   varchar(255) not null
);

comment
    on table telemechanic.position is 'Должность';

comment
    on column telemechanic.position.id is 'Идентификатор записи о должности';

comment
    on column telemechanic.position.active is 'Флаг активности';

comment
    on column telemechanic.position.organization_id is 'Идентификатор записи об организации';

comment
    on column telemechanic.position.position_name is 'Наименование позиции';

create table telemechanic.employee
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
            references telemechanic.department,
    position_id       uuid         not null
        constraint employee_position_id_fk
            references telemechanic.position
);

comment
    on table telemechanic.employee is 'Сотрудник';

comment
    on column telemechanic.employee.id is 'Идентификатор записи о сотруднике';

comment
    on column telemechanic.employee.active is 'Флаг активности';

comment
    on column telemechanic.employee.human_readable_id is 'Человекочитаемый идентификатор';

comment
    on column telemechanic.employee.first_name is 'Имя';

comment
    on column telemechanic.employee.last_name is 'Фамилия';

comment
    on column telemechanic.employee.patronymic is 'Отчество';

comment
    on column telemechanic.employee.mobile_phone is 'Номер телефона';

comment
    on column telemechanic.employee.personnel_number is 'Табельный номер';

comment
    on column telemechanic.employee.user_id is 'Идентификатор записи с таблицы corporate.user';

comment
    on column telemechanic.employee.department_id is 'Идентификатор записи о подразделении';

comment
    on column telemechanic.employee.position_id is 'Идентификатор записи о должности';


create table telemechanic.urls
(
    id      uuid not null
        constraint users_urls_pkey
            primary key,
    url     text not null,
    pattern text not null,
    method  text not null
);

comment
    on table telemechanic.urls is 'Ссылка';

comment
    on column telemechanic.urls.id is 'Идентификатор записи ссылки';

comment
    on column telemechanic.urls.url is 'Адрес ссылки';

comment
    on column telemechanic.urls.pattern is 'Паттерн ссылки';

comment
    on column telemechanic.urls.method is 'Наименование REST метода';

create table telemechanic.roles
(
    role   text not null,
    url_id uuid not null
        constraint role_urls_fkey
            references telemechanic.urls,
    constraint user_roles_ukey
        unique (url_id, role)
);

comment
    on table telemechanic.roles is 'Связка роль-ссылка';

comment
    on column telemechanic.roles.role is 'Наименование роли';

comment
    on column telemechanic.roles.url_id is 'Идентификатор записи о ссылке';

create table telemechanic.request_check_list
(
    id                uuid         not null
        constraint request_check_list_pkey
            primary key,
    human_readable_id varchar(255) not null,
    author_id         uuid not null
        constraint request_check_list_employee_id_fk
            references telemechanic.employee,
    creation_time      timestamp not null,
    vehicle_id        uuid
        constraint request_check_list_vehicle_id_fk
            references telemechanic.vehicle,
    status varchar(255) not null
);

create table telemechanic.check
(
    id                uuid         not null
        constraint check_pkey
            primary key,
    status varchar(255) not null,
    check_type   varchar(255) not null,
    attempt     int default 0 not null,
    request_check_list_id uuid not null
        constraint check_request_check_list_id_fk
            references telemechanic.request_check_list,
    constraint unique_type_request unique (check_type, request_check_list_id)
);