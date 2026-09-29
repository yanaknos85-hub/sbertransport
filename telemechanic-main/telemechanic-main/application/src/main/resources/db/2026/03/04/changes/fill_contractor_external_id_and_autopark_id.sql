do $$
    declare
        contarctors_exists integer;
        dispatcher_exists integer;
    begin
        select count(*) from information_schema.schemata where schema_name = 'contractors' into contarctors_exists;
        select count(*) from information_schema.schemata where schema_name = 'dispatcher' into dispatcher_exists;
        if contarctors_exists = 1 then
            update telemechanic.organization torg set contractor_external_id = cc.external_id
            from contractors.contractor cc
                 join contractors.contractor_organization cco on cc.id = cco.contractor_id
            where cc."service_type" = 'INTERNAL_AUTO_PARK'
            and cc.active is true
            and cco.organization_id = torg.id;
        end if;
        if dispatcher_exists = 1 then
            update telemechanic.department td set autopark_id = da.id
            from dispatcher.autopark da where td.id = da.routing_id;
        end if;
    end $$;