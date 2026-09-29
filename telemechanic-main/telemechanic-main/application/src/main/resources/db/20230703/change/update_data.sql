delete from telemechanic.roles;
delete from telemechanic.urls;

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
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''POST /request/create/'', ''ROLE_DRIVER'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /request/{requestId}/'', ''ROLE_DRIVER'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''POST /request/{requestId}/status/{status}/'', ''ROLE_DRIVER'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /request/{requestId}/{checkType}/'', ''ROLE_DRIVER'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''POST /request/{requestId}/{checkType}/status/{status}/'', ''ROLE_DRIVER'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''POST /request/{requestId}/{checkType}/check/'', ''ROLE_DRIVER'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /request/active/'', ''ROLE_DRIVER'', true)';
        END IF;
    END
$do$;
