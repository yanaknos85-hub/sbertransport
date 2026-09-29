insert into roles ."role" (code, "name", description, data_master, default_for, "exclusive")
values ('ROLE_FEDERAL_DISPATCHER_CONTRACTOR',
'Федеральный диспетчер',
'Глобальная административная роль для пользователей, которые осуществляют контроль и планирование в масштабах всего Банка и всех его автопарков.',
false,
'[]'::json,
'["INTERNAL"]'::json)
on conflict do nothing;

insert into sudir."role" (code, "name", description, data_master, default_for)
values ('ROLE_FEDERAL_DISPATCHER_CONTRACTOR',
'Федеральный диспетчер',
'Глобальная административная роль для пользователей, которые осуществляют контроль и планирование в масштабах всего Банка и всех его автопарков.',
false,
'[]'::json)
on conflict do nothing;

do
$$
    declare
        url_id uuid;
        new_role varchar(255);
    begin
        select 'ROLE_FEDERAL_DISPATCHER_CONTRACTOR' into new_role;
        for url_id in select r.url_id from dispatcher.urls u join dispatcher.roles r on u.id = r.url_id
        where r."role" in ('ROLE_MAIN_DISPATCHER_CONTRACTOR', 'ROLE_DISPATCHER_CONTRACTOR', 'ROLE_DISPATCHER_ROOM_ADMIN') loop
            insert into dispatcher.roles ("role", url_id) values (new_role, url_id) on conflict do nothing;
        end loop;
    end
$$;

do
$$
    declare
        url_id uuid;
        new_role varchar(255);
    begin
        select 'ROLE_FEDERAL_DISPATCHER_CONTRACTOR' into new_role;
        for url_id in select r.url_id from trips.urls u join trips.roles r on u.id = r.url_id
        where r."role" in ('ROLE_MAIN_DISPATCHER_CONTRACTOR', 'ROLE_DISPATCHER_CONTRACTOR', 'ROLE_DISPATCHER_ROOM_ADMIN') loop
            insert into trips.roles ("role", url_id) values (new_role, url_id) on conflict do nothing;
        end loop;
    end
$$;

do
$$
    declare
        url_id uuid;
        new_role varchar(255);
    begin
        select 'ROLE_FEDERAL_DISPATCHER_CONTRACTOR' into new_role;
        for url_id in select r.url_id from trips_cargo.urls u join trips_cargo.roles r on u.id = r.url_id
        where r."role" in ('ROLE_MAIN_DISPATCHER_CONTRACTOR', 'ROLE_DISPATCHER_CONTRACTOR', 'ROLE_DISPATCHER_ROOM_ADMIN') loop
            insert into trips_cargo.roles ("role", url_id) values (new_role, url_id) on conflict do nothing;
        end loop;
    end
$$;

do
$$
    declare
        url_id uuid;
        new_role varchar(255);
    begin
        select 'ROLE_FEDERAL_DISPATCHER_CONTRACTOR' into new_role;
        for url_id in select r.url_id from trips_reports.urls u join trips_reports.roles r on u.id = r.url_id
        where r."role" in ('ROLE_MAIN_DISPATCHER_CONTRACTOR', 'ROLE_DISPATCHER_CONTRACTOR', 'ROLE_DISPATCHER_ROOM_ADMIN') loop
            insert into trips_reports.roles ("role", url_id) values (new_role, url_id) on conflict do nothing;
        end loop;
    end
$$;

do
$$
    declare
        url_id uuid;
        new_role varchar(255);
    begin
        select 'ROLE_FEDERAL_DISPATCHER_CONTRACTOR' into new_role;
        for url_id in select r.url_id from trips_analytics.urls u join trips_analytics.roles r on u.id = r.url_id
        where r."role" in ('ROLE_MAIN_DISPATCHER_CONTRACTOR', 'ROLE_DISPATCHER_CONTRACTOR', 'ROLE_DISPATCHER_ROOM_ADMIN') loop
            insert into trips_analytics.roles ("role", url_id) values (new_role, url_id) on conflict do nothing;
        end loop;
    end
$$;

do
$$
    declare
        url_id uuid;
        new_role varchar(255);
    begin
        select 'ROLE_FEDERAL_DISPATCHER_CONTRACTOR' into new_role;
        for url_id in select r.url_id from driver_track.urls u join driver_track.roles r on u.id = r.url_id
        where r."role" in ('ROLE_MAIN_DISPATCHER_CONTRACTOR', 'ROLE_DISPATCHER_CONTRACTOR', 'ROLE_DISPATCHER_ROOM_ADMIN') loop
            insert into driver_track.roles ("role", url_id) values (new_role, url_id) on conflict do nothing;
        end loop;
    end
$$;

do
$$
    declare
        url_id uuid;
        new_role varchar(255);
    begin
        select 'ROLE_FEDERAL_DISPATCHER_CONTRACTOR' into new_role;
        for url_id in select r.url_id from auto_analytics.urls u join auto_analytics.roles r on u.id = r.url_id
        where r."role" in ('ROLE_MAIN_DISPATCHER_CONTRACTOR', 'ROLE_DISPATCHER_CONTRACTOR', 'ROLE_DISPATCHER_ROOM_ADMIN') loop
            insert into auto_analytics.roles ("role", url_id) values (new_role, url_id) on conflict do nothing;
        end loop;
    end
$$;

do
$$
    declare
        url_id uuid;
        new_role varchar(255);
    begin
        select 'ROLE_FEDERAL_DISPATCHER_CONTRACTOR' into new_role;
        for url_id in select r.url_id from contractors.urls u join contractors.roles r on u.id = r.url_id
        where r."role" in ('ROLE_DISPATCHER_ROOM_ADMIN') loop
            insert into contractors.roles ("role", url_id) values (new_role, url_id) on conflict do nothing;
        end loop;
    end
$$;

do
$$
    declare
        url_id uuid;
        new_role varchar(255);
    begin
        select 'ROLE_FEDERAL_DISPATCHER_CONTRACTOR' into new_role;
        for url_id in select r.url_id from vehicle.urls u join vehicle.roles r on u.id = r.url_id
        where r."role" in ('ROLE_MAIN_DISPATCHER_CONTRACTOR', 'ROLE_DISPATCHER_CONTRACTOR', 'ROLE_DISPATCHER_ROOM_ADMIN') loop
            insert into vehicle.roles ("role", url_id) values (new_role, url_id) on conflict do nothing;
        end loop;
    end
$$;