DO
$do$
    declare
        rec record;
    BEGIN
        FOR rec IN select change_time, request_id from telemechanic.request_history where request_status = 'ON_THE_LINE'
            LOOP
                update telemechanic.request_history
                set request_status = 'FINISHED'
                where request_id = rec.request_id
                  and change_time > rec.change_time
                  and request_status = 'EXPIRED';
            END LOOP;
    END
$do$;