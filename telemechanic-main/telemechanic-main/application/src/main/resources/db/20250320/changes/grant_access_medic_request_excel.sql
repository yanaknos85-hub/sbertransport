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
            EXECUTE 'call migrations.fill_roles(''telemechanic'',  ''GET /files/medic-request-all-organizations'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'',  ''GET /files/medic-request-all-organizations/empty/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'',  ''GET /files/medic-request-all-organizations/result/{fileName}/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';

            EXECUTE 'call migrations.fill_roles(''telemechanic'',  ''GET /files/medic-request-self-organization'', ''ROLE_ADMIN_CORP_CLIENT'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'',  ''GET /files/medic-request-self-organization/empty/'', ''ROLE_ADMIN_CORP_CLIENT'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'',  ''GET /files/medic-request-self-organization/result/{fileName}/'', ''ROLE_ADMIN_CORP_CLIENT'', true)';

            EXECUTE 'call migrations.fill_roles(''telemechanic'',  ''GET /files/medic-request-self-organization'', ''ROLE_ENGINEER_CORP_CLIENT'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'',  ''GET /files/medic-request-self-organization/empty/'', ''ROLE_ENGINEER_CORP_CLIENT'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'',  ''GET /files/medic-request-self-organization/result/{fileName}/'', ''ROLE_ENGINEER_CORP_CLIENT'', true)';
        END IF;
    END
$do$;