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
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''POST /report/'', ''ROLE_ADMIN_DATA_MASTER'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''POST /report/'', ''ROLE_ADMIN_CORP_CLIENT'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''POST /report/'', ''ROLE_ENGINEER_CORP_CLIENT'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''POST /report/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /files/registry/'', ''ROLE_ADMIN_DATA_MASTER'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /files/registry/'', ''ROLE_ADMIN_CORP_CLIENT'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /files/registry/'', ''ROLE_ENGINEER_CORP_CLIENT'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /files/registry/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /files/registry/empty/'', ''ROLE_ADMIN_DATA_MASTER'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /files/registry/empty/'', ''ROLE_ADMIN_CORP_CLIENT'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /files/registry/empty/'', ''ROLE_ENGINEER_CORP_CLIENT'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /files/registry/empty/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /files/registry/result/{fileName}/'', ''ROLE_ADMIN_DATA_MASTER'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /files/registry/result/{fileName}/'', ''ROLE_ADMIN_CORP_CLIENT'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /files/registry/result/{fileName}/'', ''ROLE_ENGINEER_CORP_CLIENT'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /files/registry/result/{fileName}/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';
        END IF;
    END
$do$;