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
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''POST /ewb/search'', ''ROLE_DISPATCHER_CONTRACTOR'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''POST /ewb/search'', ''ROLE_MAIN_DISPATCHER_CONTRACTOR'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''POST /ewb/search'', ''ROLE_DISPATCHER_ROOM_ADMIN'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /dispatchers/self'', ''ROLE_DISPATCHER_CONTRACTOR'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /dispatchers/self'', ''ROLE_MAIN_DISPATCHER_CONTRACTOR'', true)';
        END IF;
    END
$do$;