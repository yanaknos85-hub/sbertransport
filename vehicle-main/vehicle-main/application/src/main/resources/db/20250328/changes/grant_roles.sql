delete from vehicle.roles where role = 'ROLE_TELEMECHANIС';

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
            EXECUTE 'call migrations.fill_roles(''vehicle'', ''POST /transport/statenumber'', ''ROLE_TELEMECHANIC'', true)';
        END IF;
    END
$do$;