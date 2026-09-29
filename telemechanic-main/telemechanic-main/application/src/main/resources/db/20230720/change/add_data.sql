update telemechanic.employee e
set organization_id = (select d.organization_id
                       from telemechanic.department d
                       where d.id = (select ee.department_id from telemechanic.employee ee where ee.id = e.id));

alter table telemechanic.employee
    alter column organization_id set not null;
alter table telemechanic.request_check_list
    drop telemechanic_id;
alter table telemechanic.request_check_list
    drop take_to_work_time;

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
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''POST /monitoring/'', ''ROLE_TELEMECHANIC'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''PATCH /monitoring/{requestId}'', ''ROLE_TELEMECHANIC'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /monitoring/{requestId}'', ''ROLE_TELEMECHANIC'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /monitoring/photo/{photoId}'', ''ROLE_TELEMECHANIC'', true)';
        END IF;
    END
$do$;

alter table telemechanic."check"
    add comment varchar(255);

comment on column telemechanic."check".comment is 'Комментарий';

alter table telemechanic.request_check_list
    add comment varchar(255);

comment on column telemechanic.request_check_list.comment is 'Комментарий';