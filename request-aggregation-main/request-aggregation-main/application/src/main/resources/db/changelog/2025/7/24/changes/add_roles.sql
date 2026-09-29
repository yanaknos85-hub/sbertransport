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
            EXECUTE 'call migrations.fill_roles(''request_aggregation'', ''POST /manager/leads/{userId}/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';
            EXECUTE 'call migrations.fill_roles(''request_aggregation'', ''POST /manager/leads/{userId}/'', ''ROLE_ENGINEER_CORP_CLIENT'', true)';
            EXECUTE 'call migrations.fill_roles(''request_aggregation'', ''POST /manager/leads/{userId}/file/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';
            EXECUTE 'call migrations.fill_roles(''request_aggregation'', ''POST /manager/leads/{userId}/file/'', ''ROLE_ENGINEER_CORP_CLIENT'', true)';
            EXECUTE 'call migrations.fill_roles(''request_aggregation'', ''POST /manager/leads/file/validate/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';
            EXECUTE 'call migrations.fill_roles(''request_aggregation'', ''POST /manager/leads/file/validate/'', ''ROLE_ENGINEER_CORP_CLIENT'', true)';
        END IF;
    END
$do$;
