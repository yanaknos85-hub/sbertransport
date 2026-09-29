DO
$do$
    BEGIN
        IF EXISTS(
                SELECT table_name
                FROM information_schema.tables
                  where table_schema = 'corporate'
                  and table_name = 'organization'
            ) THEN
            insert into tariff.organization (id, digit_id, active, name)
            select co.id, co.digit_id, CASE WHEN co.status = 'ACTIVE' THEN true ELSE false END, co.official_name
            from corporate.organization co
            on conflict do nothing;
        END IF;
    END
$do$;

DO
$do$
    DECLARE
        rec record;
    BEGIN
        	FOR rec IN select id, digit_id, status, official_name from corporate.organization
            	LOOP
                	update tariff.organization set digit_id = rec.digit_id,
                                                "name" = rec.official_name,
                                                active = (select CASE WHEN rec.status = 'ACTIVE' THEN true ELSE false END)
                	where id = rec.id;
            	END LOOP;
    END
$do$;