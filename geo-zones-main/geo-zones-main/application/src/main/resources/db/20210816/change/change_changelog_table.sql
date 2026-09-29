do
$$
    begin
        if (select exists(select *
                          from information_schema.tables
                          where table_catalog = 'transport' and table_schema = 'public' and table_name = 'databasechangelog'))
        then
            insert into
                public.changelog_geo_zones
            select
                *
            from
                public.databasechangelog
            where
                    id ilike 'geo-zone/%' or id ilike '%/geo-zone'
               or id ilike 'authomatic-geo_zones/%'
            ;
        end if;
    end;
$$;