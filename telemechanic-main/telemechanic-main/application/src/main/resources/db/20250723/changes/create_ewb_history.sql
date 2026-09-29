create table telemechanic.ewb_history
(
    id                    uuid         not null
        constraint ewb_history_pkey
            primary key,
    change_time           timestamp    not null,
    status        varchar(50) not null,
    old_status        varchar(50),
    comment               varchar(255),
    initiator_id          uuid         not null
        constraint ewb_history_employee_id_fk
            references telemechanic.employee,
    ewb_id            uuid         not null
        constraint ewb_history_id_fk
            references telemechanic.ewb
);

comment
    on table telemechanic.ewb_history is 'История изменения ЭПЛ';

comment
    on column telemechanic.ewb_history.id is 'Идентификатор записи об истории изменения ЭПЛ';

comment
    on column telemechanic.ewb_history.change_time is 'Дата и время изменения ЭПЛ';

comment
    on column telemechanic.ewb_history.status is 'Новый статус ЭПЛ';

comment
    on column telemechanic.ewb_history.old_status is 'Старый статус ЭПЛ';

comment
    on column telemechanic.ewb_history.comment is 'Комментарий к изменению ЭПЛ';

comment
    on column telemechanic.ewb_history.initiator_id is 'Идентификатор записи об инициаторе изменения';

comment
    on column telemechanic.ewb_history.ewb_id is 'Идентификатор ЭПЛ';

create index ewb_history_id_index
    on telemechanic.ewb_history (ewb_id);

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