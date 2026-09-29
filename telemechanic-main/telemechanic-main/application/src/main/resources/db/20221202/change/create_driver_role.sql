DO
$do$
    BEGIN
        IF EXISTS(
                SELECT *
                FROM information_schema.schemata
                WHERE schema_name = 'sudir'
            ) THEN

            INSERT INTO sudir."role" (code, "default", "name", description, data_master)
            VALUES ('ROLE_DRIVER', false, 'Водитель', 'Роль, описывающая возможности водителя', false);


        END IF;
    END
$do$;


DO
$do$
    BEGIN
        IF EXISTS(
                SELECT *
                FROM information_schema.schemata
                WHERE schema_name = 'authentication'
            ) THEN

            INSERT INTO authentication."role" (code, "default", "name", description, data_master)
            VALUES ('ROLE_DRIVER', false, 'Водитель', 'Роль, описывающая возможности водителя', false);


        END IF;
    END
$do$;






DO
$do$
    BEGIN
        IF EXISTS(
                SELECT routine_schema,
                       routine_name,
                       routine_type
                FROM information_schema.routines
                WHERE routine_name = 'fill_roles'
                  and routine_schema = 'migrations'
                  and routine_type = 'PROCEDURE'
            ) THEN
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''POST /request/'', ''ROLE_DRIVER'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /request/'', ''ROLE_DRIVER'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''POST /{requestId}/status/{status}/'', ''ROLE_DRIVER'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /{requestId}/{checkType}/'', ''ROLE_DRIVER'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''POST /{requestId}/{checkType}/status/{status}/'', ''ROLE_DRIVER'', true)';

        END IF;
    END
$do$;
