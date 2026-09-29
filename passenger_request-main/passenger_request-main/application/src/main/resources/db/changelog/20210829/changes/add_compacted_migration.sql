do
$$
    begin
        if (select exists(select *
                          from information_schema.tables
                          where table_catalog = 'transport' and table_schema = 'public' and table_name = 'databasechangelog'))
        then
            if (select exists(select * from public.databasechangelog where id ilike 'request/%')) then
                insert into public.changelog_request (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, liquibase, deployment_id)
                values ('20210829-1', 'medvedev-ad', 'classpath:db/changelog/20210829/changelog.yml', now(), 2, 'EXECUTED', '8:847224893d1cf0011d144262171f0b68', 'sqlFile', '', '3.8.9', (select max(deployment_id)::bigint + 1 from public.databasechangelog));

                insert into public.changelog_request select * from public.databasechangelog where id = 'authomatic-request/20210520';
            end if;
        end if;
    end;
$$;