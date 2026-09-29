create table telemechanic.medic_request_history
(
    id                    uuid         not null
        constraint medic_request_history_pkey
            primary key,
    change_time           timestamp    not null,
    status        varchar(50) not null,
    old_status        varchar(50) not null,
    comment               varchar(255),
    initiator_id          uuid         not null
        constraint medic_request_history_employee_id_fk
            references telemechanic.employee,
    medic_request_id            uuid         not null
        constraint medic_request_history_id_fk
            references telemechanic.medic_request
);

comment
    on table telemechanic.medic_request_history is 'История изменения заявок на медосмотр';

comment
    on column telemechanic.medic_request_history.id is 'Идентификатор записи об истории изменения заявки';

comment
    on column telemechanic.medic_request_history.change_time is 'Дата и время изменения заявки';

comment
    on column telemechanic.medic_request_history.status is 'Новый статус заявки';

comment
    on column telemechanic.medic_request_history.old_status is 'Старый статус заявки';

comment
    on column telemechanic.medic_request_history.comment is 'Комментарий к изменению заявки';

comment
    on column telemechanic.medic_request_history.initiator_id is 'Идентификатор записи об инициаторе изменения';

comment
    on column telemechanic.medic_request_history.medic_request_id is 'Идентификатор записи о заявке';

create index medic_request_history_id_index
    on telemechanic.medic_request_history (medic_request_id);

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
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''PATCH /ewb/{id}/cancel'', ''ROLE_ADMIN_CORP_CLIENT'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''PATCH /ewb/{id}/cancel'', ''ROLE_ENGINEER_CORP_CLIENT'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''PATCH /ewb/{id}/cancel'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';
        END IF;
    END
$do$;