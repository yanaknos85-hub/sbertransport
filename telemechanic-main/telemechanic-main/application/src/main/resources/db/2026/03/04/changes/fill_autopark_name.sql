do $$
    declare
        dispatcher_exists integer;
    begin
        select count(*) from information_schema.schemata where schema_name = 'dispatcher' into dispatcher_exists;
        if dispatcher_exists = 1 then
            update telemechanic.department td set autopark_name = da.name
            from dispatcher.autopark da where td.id = da.routing_id;
        end if;
    end $$;