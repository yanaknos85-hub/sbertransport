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
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''POST /monitoring'', ''ROLE_ADMIN_DATA_MASTER'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''PATCH /monitoring/{requestId}/'', ''ROLE_ADMIN_DATA_MASTER'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /monitoring/{requestId}/'', ''ROLE_ADMIN_DATA_MASTER'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /monitoring/photo/{photoId}/'', ''ROLE_ADMIN_DATA_MASTER'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /request/{requestId}/status/history/'', ''ROLE_ADMIN_DATA_MASTER'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''POST /monitoring'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''PATCH /monitoring/{requestId}/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /monitoring/{requestId}/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /monitoring/photo/{photoId}/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /request/{requestId}/status/history/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''POST /monitoring'', ''ROLE_TELEMECHANIС_ORGANIZATION'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''PATCH /monitoring/{requestId}/'', ''ROLE_TELEMECHANIС_ORGANIZATION'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /monitoring/{requestId}/'', ''ROLE_TELEMECHANIС_ORGANIZATION'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /monitoring/photo/{photoId}/'', ''ROLE_TELEMECHANIС_ORGANIZATION'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /request/{requestId}/status/history/'', ''ROLE_TELEMECHANIС_ORGANIZATION'', true)';
        END IF;
    END
$do$;