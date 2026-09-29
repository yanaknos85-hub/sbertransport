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
            execute 'call migrations.fill_roles(''telemechanic'',  ''POST /ewb/form-title/2/'', ''ROLE_DISPATCHER_SUPPORT_SERVICE'', true)';
            execute 'call migrations.fill_roles(''telemechanic'',  ''POST /ewb/form-title/2/'', ''ROLE_MEDIC'', true)';
        end if;
    end
$do$;