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
EXECUTE 'call migrations.fill_roles(''vehicle'', ''POST /vehicle/search/'', ''ROLE_ENGINEER_CORP_CLIENT'', true)';
EXECUTE 'call migrations.fill_roles(''vehicle'', ''POST /vehicle/search/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';

EXECUTE 'call migrations.fill_roles(''vehicle'', ''POST /vehicle/all/'', ''ROLE_ENGINEER_CORP_CLIENT'', true)';
EXECUTE 'call migrations.fill_roles(''vehicle'', ''POST /vehicle/all/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';

EXECUTE 'call migrations.fill_roles(''vehicle'', ''POST /vehicle/'', ''ROLE_ENGINEER_CORP_CLIENT'', true)';
EXECUTE 'call migrations.fill_roles(''vehicle'', ''POST /vehicle/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';

EXECUTE 'call migrations.fill_roles(''vehicle'', ''POST /brand/all/'', ''ROLE_ENGINEER_CORP_CLIENT'', true)';
EXECUTE 'call migrations.fill_roles(''vehicle'', ''POST /brand/all/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';

EXECUTE 'call migrations.fill_roles(''vehicle'', ''POST /model/all/'', ''ROLE_ENGINEER_CORP_CLIENT'', true)';
EXECUTE 'call migrations.fill_roles(''vehicle'', ''POST /model/all/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';

EXECUTE 'call migrations.fill_roles(''vehicle'', ''GET /vehicle/{vehicleId}/'', ''ROLE_ENGINEER_CORP_CLIENT'', true)';
EXECUTE 'call migrations.fill_roles(''vehicle'', ''GET /vehicle/{vehicleId}/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';

EXECUTE 'call migrations.fill_roles(''vehicle'', ''DELETE /vehicle/{vehicleId}/'', ''ROLE_ENGINEER_CORP_CLIENT'', true)';
EXECUTE 'call migrations.fill_roles(''vehicle'', ''DELETE /vehicle/{vehicleId}/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';

EXECUTE 'call migrations.fill_roles(''vehicle'', ''PATCH /transport/deactivate/{transportId}/'', ''ROLE_ENGINEER_CORP_CLIENT'', true)';
EXECUTE 'call migrations.fill_roles(''vehicle'', ''PATCH /transport/deactivate/{transportId}/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';

END IF;
END
$do$;


