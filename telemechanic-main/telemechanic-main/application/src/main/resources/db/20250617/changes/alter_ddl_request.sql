DO
$do$
    BEGIN
        if exists(
                select 1 from telemechanic.organization where id = '821619bd-14c8-49e5-bd5c-9e337bac2620'
            ) then
            update telemechanic.request set organization_id = '821619bd-14c8-49e5-bd5c-9e337bac2620';
        else
            update telemechanic.request set organization_id = (select id from telemechanic.organization limit 1);
        end if;
    END
$do$;

alter table telemechanic.request alter column organization_id set not null;