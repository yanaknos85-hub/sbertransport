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
            execute 'call migrations.fill_roles(''telemechanic'', ''PATCH /drivers/{id}/'', ''ROLE_ADMIN_CORP_CLIENT'', true)';
            execute 'call migrations.fill_roles(''telemechanic'', ''PATCH /drivers/{id}/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';
        end if;
    end
$do$;