DO
$$
    DECLARE
        rec record;
    BEGIN
        FOR rec IN
            select tr.organization_id, tr.state_number
            from vehicle.transport tr
            LOOP
                update telemechanic.transport tr
                set organization_id = rec.organization_id
                where tr.state_number = rec.state_number;
            END LOOP;
    END;
$$;