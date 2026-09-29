
DO
$$
    DECLARE
        rec record;
    BEGIN
        FOR rec IN
            select r.url_id, r.role
            from telemechanic.urls u
            join telemechanic.roles r on u.id = r.url_id
            where r.role like 'ROLE_TELEMECHANIС%'
            LOOP
                IF rec.role = 'ROLE_TELEMECHANIС' THEN
                    update telemechanic.roles r
                    set role = 'ROLE_TELEMECHANIC'
                    where r.url_id = rec.url_id
                        and r.role = 'ROLE_TELEMECHANIС';
                END IF;
                IF rec.role = 'ROLE_TELEMECHANIС_ORGANIZATION' THEN
                    update telemechanic.roles r
                    set role = 'ROLE_TELEMECHANIC_ORGANIZATION'
                    where r.url_id = rec.url_id
                        and r.role = 'ROLE_TELEMECHANIС_ORGANIZATION';
                END IF;
            END LOOP;
    END;
$$