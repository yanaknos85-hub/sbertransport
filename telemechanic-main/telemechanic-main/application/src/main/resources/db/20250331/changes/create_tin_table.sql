create table if not exists telemechanic.tin(
    id uuid primary key,
    employee_id uuid not null,
    tin varchar(12) not null,

    constraint fk_employee_tin foreign key (employee_id) references telemechanic.employee(id)
);

comment on table telemechanic.tin is 'ИНН сотрудника';
comment on column telemechanic.tin.employee_id is 'Идентификатор сотрудника';
comment on column telemechanic.tin.tin is 'ИНН';