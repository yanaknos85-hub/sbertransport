DO
$$
    DECLARE
        rec record;
    BEGIN
        FOR rec IN
            select r.id, max(cp.creation_time) as change_time
            from telemechanic.request r
            join telemechanic."check" c on r.id = c.request_id
            left join telemechanic.check_photo cp on c.id = cp.check_id
            where r.checks_finished_time is null
            and (select count(*)
            	 from telemechanic."check" c
            	 where  r.id = c.request_id
            	 and c.attempt > 0) = 21
            group by r.id
            LOOP
                update telemechanic.request r
                set checks_finished_time = rec.change_time
                where r.id = rec.id;
            END LOOP;
    END;
$$