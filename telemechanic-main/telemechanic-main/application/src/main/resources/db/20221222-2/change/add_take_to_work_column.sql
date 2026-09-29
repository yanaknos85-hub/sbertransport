

alter table telemechanic.request_check_list add column take_to_work_time timestamp;

COMMENT ON COLUMN telemechanic.request_check_list.take_to_work_time IS 'Дата и время взятия в работу заявки';

alter table telemechanic.request_check_list
    add telemechanic_id uuid;

comment on column telemechanic.request_check_list.telemechanic_id is 'Телемеханик';

alter table telemechanic.request_check_list
    add constraint request_check_list_employee_id_fk_3
        foreign key (telemechanic_id) references telemechanic.employee;



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
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''POST /request/{requestId}/take_to_work/'', ''ROLE_TELEMECHANIC'', true)';
        END IF;
    END
$do$;