do
$$
    begin
        if (select exists(select *
                          from information_schema.tables
                          where table_catalog = 'transport' and table_schema = 'public' and table_name = 'databasechangelog'))
        then
            if (select exists(select * from public.databasechangelog where id ilike 'address%')) then
                insert into public.changelog_addresses (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, liquibase, deployment_id)
                values ('20211222-1', 'medvedev-ad', 'classpath:db/changelog/20211222/changelog.yml', now(), 2, 'EXECUTED',
                        '8:37533ca4ed6d05777f1899fcda62247d', 'sqlFile', '', '3.8.9', 1);

                insert into public.changelog_addresses select * from public.databasechangelog where id like 'authomatic-address*';
            end if;
        end if;
    end;
$$;