do
$$
    declare
        rol authentication.role;
    begin
        for rol in select * from authentication.role
            loop
                call migrations.fill_roles('push', 'POST /token/{recipientId}/', rol.code, true);
            end loop;
    end;
$$;