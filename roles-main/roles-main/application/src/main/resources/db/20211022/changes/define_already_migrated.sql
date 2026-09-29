DO
$$
    begin
        if (select exists(select *
                          from information_schema.tables
                          where table_catalog = 'transport' and table_schema = 'public' and table_name = 'databasechangelog'))
        then
            if (select exists(select * from public.databasechangelog where id ilike 'role%')) then
                insert into public.changelog_roles (id, author, filename, dateexecuted, orderexecuted, exectype,
                                                    md5sum, description, comments, liquibase, deployment_id)
                values ('20211022-1', 'medvedev-ad', 'classpath:db/20211022/changelog.yml', now(), 2,
                        'EXECUTED', '8:16a4357aba7da7656dd945ba71218f4d', 'sqlFile', '', '3.8.9', 1);

                insert into public.changelog_roles select * from public.databasechangelog where id like
                                                                                                'authomatic-roles/%';
            end if;
        end if;
    end;
$$;