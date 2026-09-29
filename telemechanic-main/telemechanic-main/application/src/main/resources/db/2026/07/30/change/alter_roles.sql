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
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /department'', ''ROLE_MAIN_DISPATCHER_CONTRACTOR'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /department'', ''ROLE_DISPATCHER_CONTRACTOR'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /organization/internal-autopark'', ''ROLE_MAIN_DISPATCHER_CONTRACTOR'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /organization/internal-autopark'', ''ROLE_DISPATCHER_CONTRACTOR'', true)';
        END IF;
    END
$do$;