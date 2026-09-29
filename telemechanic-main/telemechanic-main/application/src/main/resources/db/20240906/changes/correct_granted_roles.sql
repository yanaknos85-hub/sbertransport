delete from telemechanic.roles vr where vr.url_id in (select id from telemechanic.urls where url = '/ewb/auth/');

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
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''POST /ewb/auth'', ''ROLE_ADMIN_DATA_MASTER'', true)';
        END IF;
    END
$do$;