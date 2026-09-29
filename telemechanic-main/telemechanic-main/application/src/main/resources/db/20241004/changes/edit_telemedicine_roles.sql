DO
$$
    DECLARE
        rec record;
    BEGIN
        FOR rec IN
            select u.id
            from telemechanic.urls u
            where u.url like '/telemedicine%'
            LOOP
                update telemechanic.roles r
                set role = 'ROLE_MEDIC'
                where r.url_id = rec.id
                  and r.role = 'ROLE_TELEMECHANIC';
            END LOOP;
    END;
$$