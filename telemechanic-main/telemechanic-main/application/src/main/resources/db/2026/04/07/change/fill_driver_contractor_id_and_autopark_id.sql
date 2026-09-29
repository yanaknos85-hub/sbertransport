do $$
    declare
        dispatcher_exists integer;
    begin
        select count(*) from information_schema.schemata where schema_name = 'dispatcher' into dispatcher_exists;
        if dispatcher_exists = 1 then
            update telemechanic.driver td set autopark_id = dd.autopark_id, contractor_id = dd.contractor_id
            from dispatcher.driver dd where td.employee_id = dd.oauth_id
            and dd.oauth_id is not null;
        end if;
    end $$;
