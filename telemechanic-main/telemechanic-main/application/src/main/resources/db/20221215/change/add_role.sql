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
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''POST /request/create/'', ''ROLE_PARKING_ADMIN'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''POST /request/create/'', ''ROLE_ADMIN_DATA_MASTER'', true)';

        END IF;
    END
$do$;