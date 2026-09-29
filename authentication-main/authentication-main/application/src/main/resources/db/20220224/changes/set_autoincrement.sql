alter table authentication_aud.revinfo
    alter column id type int using nextval('authentication_aud.hibernate_sequence');

do
$$
    declare
        max_id int;
        stmt text;
    begin
        select max(id) into max_id from authentication_aud.revinfo;
        if max_id is not null and max_id > 0 then
            stmt = 'alter sequence authentication_aud.hibernate_sequence restart with ' || (max_id + 10) || ';';
            execute stmt;
        end if;
    end;
$$;