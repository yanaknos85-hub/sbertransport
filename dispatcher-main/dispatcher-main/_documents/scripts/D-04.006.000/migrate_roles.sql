
do
$$
    declare
        url_id uuid;
		new_role varchar(255);
    begin
		select 'ROLE_DISPATCHER_ROOM_ADMIN' into new_role;
        for url_id in select r.url_id from dispatcher.urls u join dispatcher.roles r on u.id = r.url_id where r."role" = 'ROLE_DISPATCHER_CONTRACTOR' loop
               insert into dispatcher.roles ("role", url_id) values (new_role, url_id);
        end loop;
    end
$$;

do
$$
    declare
        url_id uuid;
		new_role varchar(255);
    begin
		select 'ROLE_DISPATCHER_ROOM_ADMIN' into new_role;
        for url_id in select r.url_id from trips.urls u join trips.roles r on u.id = r.url_id where r."role" = 'ROLE_DISPATCHER_CONTRACTOR' loop
               insert into trips.roles ("role", url_id) values (new_role, url_id);
        end loop;
    end
$$;

do
$$
    declare
        url_id uuid;
		new_role varchar(255);
    begin
		select 'ROLE_DISPATCHER_ROOM_ADMIN' into new_role;
        for url_id in select r.url_id from trips_cargo.urls u join trips_cargo.roles r on u.id = r.url_id where r."role" = 'ROLE_DISPATCHER_CONTRACTOR' loop
               insert into trips_cargo.roles ("role", url_id) values (new_role, url_id);
        end loop;
    end
$$;

do
$$
    declare
        url_id uuid;
		new_role varchar(255);
    begin
		select 'ROLE_DISPATCHER_ROOM_ADMIN' into new_role;
        for url_id in select r.url_id from trips_reports.urls u join trips_reports.roles r on u.id = r.url_id where r."role" = 'ROLE_DISPATCHER_CONTRACTOR' loop
               insert into trips_reports.roles ("role", url_id) values (new_role, url_id);
        end loop;
    end
$$;