do
$$
    begin
        if (select exists(select *
                          from information_schema.tables
                          where table_schema = 'public' and table_name = 'databasechangelog'))
        then
            delete
            from public.databasechangelog
            where id ilike 'authentication/%'
               or id ilike '%/authentication'
               or id ilike 'authomatic-authentication/%';
        end if;
    end;
$$;