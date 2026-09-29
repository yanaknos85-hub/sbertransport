do
$$
    begin
        if (select exists(select *
                          from information_schema.tables
                          where table_schema = 'public' and table_name = 'databasechangelog'))
        then
            insert into
                public.changelog_tariff
            select
                *
            from
                public.changelog_tariff
            where
                    id ilike 'tariff/%' or id ilike '%/tariff'
               or id = 'request/20200829' or id = 'request/20200910'
               or id = 'request/20201001'
               or id = 'request/20201025'
               or id = 'request/20201117'
               or id ilike 'authomatic-tariff/%';
        end if;

        if (select exists(select *
                          from information_schema.tables
                          where table_schema = 'tariff' and table_name = 'databasechangelog'))
        then
            insert into
                public.changelog_tariff
            select
                *
            from
                tariff.databasechangelog
            where
                id is not null;
        end if;
    end;
$$;