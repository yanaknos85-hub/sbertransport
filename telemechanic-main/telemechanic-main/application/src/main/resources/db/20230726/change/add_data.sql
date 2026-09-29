alter table if exists telemechanic.request_check_list
    rename to request;
alter table if exists telemechanic.request
    rename constraint request_check_list_employee_id_fk to request_employee_id_fk;
alter table if exists telemechanic.request
    rename constraint request_check_list_vehicle_id_fk to request_vehicle_id_fk;
alter index if exists telemechanic.request_check_list_pkey rename to request_pkey;
alter table if exists telemechanic."check"
    rename column request_check_list_id to request_id;
alter table if exists telemechanic."check"
    rename constraint check_request_check_list_id_fk to check_request_cid_fk;
comment on table telemechanic.request is 'Заявка';

create table telemechanic.request_history
(
    id                    uuid         not null
        constraint request_history_pkey
            primary key,
    change_time           timestamp    not null,
    request_status        varchar(255) not null,
    comment               varchar(255),
    initiator_id          uuid         not null
        constraint request_history_employee_id_fk
            references telemechanic.employee,
    request_id            uuid         not null
        constraint request_history_request_id_fk
            references telemechanic.request
);

comment
    on table telemechanic.request_history is 'История изменения заявок';

comment
    on column telemechanic.request_history.id is 'Идентификатор записи об истории изменении заявки';

comment
    on column telemechanic.request_history.change_time is 'Дата и время изменения заявки';

comment
    on column telemechanic.request_history.request_status is 'Статус заявки';

comment
    on column telemechanic.request_history.comment is 'Комментарий к изменению заявки';

comment
    on column telemechanic.request_history.initiator_id is 'Идентификатор записи об инициаторе изменения';

comment
    on column telemechanic.request_history.request_id is 'Идентификатор записи о заявке';

create index request_history_request_id_index
    on telemechanic.request_history (request_id);

create index department_organization_id_index
    on telemechanic.department (organization_id);

create index department_parent_id_index
    on telemechanic.department (parent_id);

create index employee_department_id_index
    on telemechanic.employee (department_id);

create index employee_position_id_index
    on telemechanic.employee (position_id);

create index request_creation_time_index
    on telemechanic.request (creation_time);

create index request_vehicle_id_index
    on telemechanic.request (vehicle_id);

create index check_photo_check_id_index
    on telemechanic.check_photo (check_id);

DO
$do$
    BEGIN
        IF EXISTS(
                SELECT routine_schema,
                       routine_name,
                       routine_type
                FROM information_schema.routines
                WHERE routine_name = 'fill_roles'
                  and routine_schema = 'migrations'
                  and routine_type = 'PROCEDURE'
            ) THEN
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /request/{requestId}/status/history'', ''ROLE_DRIVER'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /request/{requestId}/status/history'', ''ROLE_TELEMECHANIC'', true)';
        END IF;
    END
$do$;