DO
$$
    DECLARE
        rec record;
    BEGIN
        FOR rec IN
            select dl.*
            from vehicle.driving_license dl
            LOOP
                insert into telemechanic.driving_license(id, series, number, issue_date, expiry_date, previous_id)
                values (rec.id, rec.series, rec.number, rec.issue_date, rec.expiry_date, rec.previous_id);
            END LOOP;
    END;
$$;