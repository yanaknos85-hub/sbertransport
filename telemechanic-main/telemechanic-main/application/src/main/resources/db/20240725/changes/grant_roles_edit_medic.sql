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
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''PATCH /medical-license/{id}'', ''ROLE_ADMIN_CORP_CLIENT'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''PATCH /medical-license/{id}'', ''ROLE_ADMIN_DATA_MASTER'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''PATCH /medical-license/{id}'', ''ROLE_ENGINEER_CORP_CLIENT'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''PATCH /medical-license/{id}'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''PATCH /medical-license/{id}'', ''ROLE_TELEMECHANIС'', true)';
        END IF;
    END
$do$;