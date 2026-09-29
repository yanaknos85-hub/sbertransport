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
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /organization/'', ''ROLE_ADMIN_DATA_MASTER'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /organization/employee/'', ''ROLE_ADMIN_DATA_MASTER'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''POST /organization/department/'', ''ROLE_ADMIN_DATA_MASTER'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /vehicle/state-number/'', ''ROLE_ADMIN_DATA_MASTER'', true)';

            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /organization/'', ''ROLE_ADMIN_CORP_CLIENT'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /organization/employee/'', ''ROLE_ADMIN_CORP_CLIENT'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''POST /organization/department/'', ''ROLE_ADMIN_CORP_CLIENT'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /vehicle/state-number/'', ''ROLE_ADMIN_CORP_CLIENT'', true)';

            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /organization/'', ''ROLE_ENGINEER_CORP_CLIENT'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /organization/employee/'', ''ROLE_ENGINEER_CORP_CLIENT'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''POST /organization/department/'', ''ROLE_ENGINEER_CORP_CLIENT'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /vehicle/state-number/'', ''ROLE_ENGINEER_CORP_CLIENT'', true)';

            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /organization/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /organization/employee/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''POST /organization/department/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /vehicle/state-number/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';
    END IF;
END
$do$;