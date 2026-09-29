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
            EXECUTE 'call migrations.fill_roles(''request_aggregation'', ''GET /manager/planner/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';
            EXECUTE 'call migrations.fill_roles(''request_aggregation'', ''GET /manager/planner/'', ''ROLE_ENGINEER_CORP_CLIENT'', true)';
        END IF;
    END
$do$;