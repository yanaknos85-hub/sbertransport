create table if not exists tariff.message_employee
(
    id               uuid           primary key,
    patronymic       varchar(255),
    user_id          uuid,
    department_id    uuid,
    organization_id    uuid,
    first_name       varchar(255),
    last_name        varchar(255)
);