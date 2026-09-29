delete from vehicle.roles where role = 'ROLE_TELEMECHANIС';

DO
$do$
    begin
        if exists(
                select routine_schema,
                       routine_name,
                       routine_type
                from information_schema.routines
                where routine_name = 'fill_roles'
                  and routine_schema = 'migrations'
                  and routine_type = 'PROCEDURE'
            ) then
            execute 'call migrations.fill_roles(''vehicle'', ''GET /organization/'', ''ROLE_ADMIN_CORP_CLIENT'', true)';
            execute 'call migrations.fill_roles(''vehicle'', ''GET /organization/'', ''ROLE_ENGINEER_CORP_CLIENT'', true)';
            execute 'call migrations.fill_roles(''vehicle'', ''GET /organization/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';

            execute 'call migrations.fill_roles(''vehicle'', ''POST /organization/department/'', ''ROLE_ADMIN_CORP_CLIENT'', true)';
            execute 'call migrations.fill_roles(''vehicle'', ''POST /organization/department/'', ''ROLE_ENGINEER_CORP_CLIENT'', true)';
            execute 'call migrations.fill_roles(''vehicle'', ''POST /organization/department/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';
        end if;
    end
$do$;