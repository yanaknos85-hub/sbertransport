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
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''PATCH /monitoring/{requestId}/ewb'', ''ROLE_TELEMECHANIC'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''PATCH /monitoring/{requestId}/ewb'', ''ROLE_ADMIN_DATA_MASTER'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''PATCH /monitoring/{requestId}/ewb'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''PATCH /monitoring/{requestId}/ewb'', ''ROLE_TELEMECHANIC_ORGANIZATION'', true)';
        END IF;
    END
$do$;