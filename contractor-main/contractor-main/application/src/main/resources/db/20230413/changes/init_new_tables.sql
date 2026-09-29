create table contractors.organization
(
    id       uuid not null constraint organization_pkey primary key,
    digit_id numeric,
    active   boolean default true not null,
    name     varchar(255)
);
comment on table  contractors.organization is 'Организации';
comment on column contractors.organization.id is 'Идентификатор';
comment on column contractors.organization.digit_id is 'Уникальный идентификатор (числовой ID)';
comment on column contractors.organization.active is 'Флаг активности(неудаленности)';
comment on column contractors.organization.name is 'Название';

create table contractors.employee
(
    id               uuid  not null constraint employee_pkey primary key,
    humanreadableid  varchar(100),
    first_name       varchar(255) default ''::character varying,
    last_name        varchar(255) default ''::character varying,
    patronymic       varchar(255),
    personnel_number varchar(255),
    user_id          uuid,
    organization_id  uuid,
    active           boolean      default true not null
);
comment on table  contractors.employee is 'Сотрудники';
comment on column contractors.employee.id is 'Идентификатор';
comment on column contractors.employee.humanreadableid is 'Человекочитаемый идентификатор';
comment on column contractors.employee.first_name is 'Имя';
comment on column contractors.employee.last_name is 'Фамилия';
comment on column contractors.employee.patronymic is 'Отчество';
comment on column contractors.employee.personnel_number is 'ТН';
comment on column contractors.employee.user_id is 'Идентификатор пользователя';
comment on column contractors.employee.organization_id is 'Идентификатор оргранизацции';
comment on column contractors.employee.active is 'Флаг активности(неудаленности)';

create table contractors.contract
(
    id            uuid not null constraint contract_pkey primary key,
    contractor_id uuid,
    organizations text,
    active        boolean default true
);
comment on table contractors.contract is 'Контракты из модуля тарифов';
comment on column contractors.contract.id is 'Идентификатор контракта';
comment on column contractors.contract.contractor_id is 'Идентификатор контрагента';
comment on column contractors.contract.organizations is 'Организации';
comment on column contractors.contract.active is 'Флаг активности';

