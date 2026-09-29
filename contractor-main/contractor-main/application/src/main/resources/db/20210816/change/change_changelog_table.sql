do
$$
    begin
        if (select exists(select *
                          from information_schema.tables
                          where table_catalog = 'transport' and table_schema = 'public' and table_name = 'databasechangelog'))
        then
            insert into
                public.changelog_contractor
            select
                *
            from
                public.databasechangelog
            where
                    id ilike 'contractor/%' or id ilike '%/contractor'
               or id ilike 'authomatic-contractors/%';
        end if;
    end;
$$;