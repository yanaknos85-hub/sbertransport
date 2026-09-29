DO
$do$
    BEGIN
        IF EXISTS(
                SELECT *
                FROM information_schema.schemata
                WHERE schema_name = 'sudir'
            ) THEN

            INSERT INTO sudir."role" (code, "default_for", "name", description, data_master)
            VALUES ('ROLE_TELEMECHANIC', null, 'Телемеханик', 'Роль, описывающая возможности телемеханика', false);


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

            INSERT INTO authentication."role" (code, "default_for", "name", description, data_master)
            VALUES ('ROLE_TELEMECHANIC', null, 'Телемеханик', 'Роль, описывающая возможности телемеханика', false);


        END IF;
    END
$do$;

