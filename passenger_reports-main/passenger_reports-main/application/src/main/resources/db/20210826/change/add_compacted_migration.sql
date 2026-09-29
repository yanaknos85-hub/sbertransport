do
$$
    begin
        if (select exists(select *
                          from information_schema.tables
                          where table_catalog = 'transport' and table_schema = 'public' and table_name = 'databasechangelog'))
        then
            if (select exists(select * from public.databasechangelog where id ilike 'reports/%')) then
                insert into public.changelog_reports (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, liquibase, deployment_id)
                values ('20210826-1', 'medvedev-ad', 'classpath:db/20210826/changelog.yml', now(), 2, 'EXECUTED', '8:0286ab609b138aa0377db4e5305d8aed', 'sqlFile', '', '3.8.9', (select max(deployment_id)::bigint + 1 from public.databasechangelog));

                insert into public.changelog_reports select * from public.databasechangelog where id = 'authomatic-reports/20210520';

                delete from public.databasechangelog
                where id in ('reports/20201126', 'reports/20201204', 'reports/20201215', 'reports/20210126', 'reports/20210128',
                            'reports/20210201', 'reports/20210204', 'reports/20210205', 'reports/20210209', 'reports/20210211',
                            'reports/20210217', 'reports/20210219', 'reports/20210303', 'reports/20210304', 'reports/20210309',
                            'reports/20210312', 'reports/20210312-1', 'reports/20210317', 'reports/20210329', 'reports/20210330',
                            'reports/20210331', 'reports/20210401', 'reports/20210406', 'reports/20210412', 'reports/20210415',
                            'reports/20210419', 'reports/20210420', 'reports/20210504', 'reports/20210514', 'reports/20210601',
                            'reports/20210607', 'reports/20210611', 'reports/20210618', 'reports/20210628', 'reports/20210629',
                            'reports/20210816', 'request/20210817');
            end if;
        end if;
    end;
$$;