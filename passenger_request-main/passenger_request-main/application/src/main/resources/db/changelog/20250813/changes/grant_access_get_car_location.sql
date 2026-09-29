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
            execute 'call migrations.fill_roles(''request'', ''GET /car-location/'', ''ROLE_EMPLOYEE_CORP_CLIENT'', true)';
            execute 'call migrations.fill_roles(''request'', ''GET /car-location/'', ''ROLE_ENGINEER_CORP_CLIENT'', true)';
        end if;
    end
$do$;