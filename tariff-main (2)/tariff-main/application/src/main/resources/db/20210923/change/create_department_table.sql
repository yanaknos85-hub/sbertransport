create table if not exists tariff.message_department
(
    id                  uuid           primary key,
    parent_id           uuid,
    organization_id     uuid,
    human_readable_id   varchar,
    department_name     varchar(255)
);