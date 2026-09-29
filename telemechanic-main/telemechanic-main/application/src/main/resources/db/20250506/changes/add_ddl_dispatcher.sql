create table if not exists telemechanic.dispatcher
(
    id                                      uuid
        constraint dispatcher_id_pk primary key,
    employee_id                           uuid            not null
        constraint dispatcher_employee_id_fk
            references telemechanic.employee (id),
    organization_id                         uuid            not null
        constraint dispatcher_organization_id_fk
            references telemechanic.organization (id),
    department_id                           uuid            not null
        constraint dispatcher_department_id_fk
            references telemechanic.department (id),
    region_code                             varchar(3)      not null,
    attorney_id                             uuid            not null,
    issue_date                              date            not null,
    expiry_date                             date            not null,
    creation_system                         varchar(150)    not null,
    active                                  boolean         not null
);

comment on table telemechanic.dispatcher is 'Диспетчер';

comment on column telemechanic.dispatcher.id is 'Идентификатор записи о диспетчере';
comment on column telemechanic.dispatcher.employee_id is 'Идентификатор диспетчера';
comment on column telemechanic.dispatcher.organization_id is 'Идентификатор организации владельца автопарка';
comment on column telemechanic.dispatcher.department_id is 'Идентификатор подразделения владельца автопарка';
comment on column telemechanic.dispatcher.region_code is 'Код региона РФ, в котором действует диспетчер';
comment on column telemechanic.dispatcher.attorney_id is 'Номер доверенности';
comment on column telemechanic.dispatcher.issue_date is 'Дата выдачи доверенности';
comment on column telemechanic.dispatcher.expiry_date is 'Дата окончания срока действия доверенности';
comment on column telemechanic.dispatcher.creation_system is 'Система создания доверенности';
comment on column telemechanic.dispatcher.active is 'Флаг активности';