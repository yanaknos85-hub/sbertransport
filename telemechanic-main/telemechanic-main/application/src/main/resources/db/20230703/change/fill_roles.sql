create schema if not exists migrations;

DO
$do$
    BEGIN
        IF NOT EXISTS(
                SELECT routine_schema,
                       routine_name,
                       routine_type
                FROM information_schema.routines
                WHERE routine_name = 'fill_roles'
                  and routine_schema = 'migrations'
                  and routine_type = 'PROCEDURE'
            ) THEN
            create procedure migrations.fill_roles(servicename text, sourceurl text, rolename text, endslash boolean)
                language plpgsql
            as
            $$
            DECLARE
                method TEXT;
                role TEXT;
                path TEXT;
                pattern TEXT;
                stmt TEXT;
                id uuid;
                checkData bool;
            BEGIN
                method = split_part(sourceUrl, ' ', 1);
                path = split_part(sourceUrl, ' ', 2);
                role = roleName;

                pattern = path;

                pattern = regexp_replace(pattern, '/\{[a-zA-Z0-9_]+\}', '/_%', 'g');

                if endSlash then
                    if pattern not like '%/' then
                        pattern = pattern || '/';
                    end if;
                    if path not like '%/' then
                        path = path || '/';
                    end if;
                else
                    if pattern like '%/' then
                        pattern = substring(pattern, 1, length(pattern)-1);
                    end if;
                    if path like '%/' then
                        path = substring(path, 1, length(path)-1);
                    end if;
                end if;

--                 RAISE NOTICE 'Adding rights for role % to url % %', role, method, path;

--                 RAISE NOTICE 'New url: %, matches pattern: %', path, pattern;

                stmt = 'SELECT id FROM ' || serviceName || '.urls WHERE pattern = ''' || pattern || ''' and method = ''' || method ||''';';

                EXECUTE stmt INTO id;

                if id is null then
                    id = md5(random()::text || clock_timestamp()::text)::uuid;
                    stmt = 'INSERT INTO ' || serviceName || '.urls (id, method, url, pattern) VALUES (''' || id || ''', ''' || method || ''', ''' || path || ''', ''' || pattern || ''');';
                    EXECUTE stmt;
                end if;

--                 RAISE NOTICE 'Found url with id: %', id;

                stmt = 'SELECT count(*) > 0 FROM ' || serviceName || '.roles WHERE url_id = ''' || id || ''' AND role = ''' || role || ''';';
                EXECUTE stmt INTO checkData;

                if checkData is true then
--                     RAISE NOTICE 'Url % % already linked to role %', method, path, role;
                else
                    stmt = 'INSERT INTO ' || serviceName || '.roles (url_id, role) VALUES (''' || id || ''', ''' || role || ''');';
                    EXECUTE stmt;
--                     RAISE NOTICE 'Url % % successfully linked to role %', method, path, role;
                end if;
            END;
            $$;
        END IF;
    END
$do$;
