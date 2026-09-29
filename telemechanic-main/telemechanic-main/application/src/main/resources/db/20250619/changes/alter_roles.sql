delete from telemechanic.roles r where r.url_id in (select id from telemechanic.urls where url like '/organization/' and method like 'GET');

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
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /organization/'', ''ROLE_ADMIN_CORP_CLIENT'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /organization/'', ''ROLE_ENGINEER_CORP_CLIENT'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /organization/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';
        END IF;
    END
$do$;