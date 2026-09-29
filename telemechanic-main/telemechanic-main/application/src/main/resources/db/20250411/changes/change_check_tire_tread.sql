DO
$$
    DECLARE
        rec record;
    BEGIN
        FOR rec IN
            select c.id as id, c.status as status, c.attempt, c.request_id as request_id
            from telemechanic.check c
            where c.check_type = 'TIRE_TREAD'
            LOOP
                insert into telemechanic.check(id, status, check_type, attempt, request_id, comment)
                values (md5(random()::text || clock_timestamp()::text)::uuid, rec.status, 'SAFETY', rec.attempt, rec.request_id, null);

                update telemechanic.check set check_type = 'WHEELS_AND_TIRES' where rec.id = id;
            END LOOP;
    END;
$$;