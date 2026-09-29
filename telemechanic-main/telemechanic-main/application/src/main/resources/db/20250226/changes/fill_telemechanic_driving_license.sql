DO
$$
    DECLARE
        rec record;
    BEGIN
        FOR rec IN
            select
                dl.id as id,
                dl.driver_id as driver_id,
                dl.series as series,
                dl.number as number,
                dl.issue_date as issue_date,
                dl.expiry_date as expiry_date,
                dl.previous_id as previous_id
            from vehicle.driving_license dl
            LOOP
                insert into telemechanic.driving_license (id, driver_id, series, number, issue_date, expiry_date, previous_id)
                values (rec.id, rec.driver_id, rec.series, rec.number, rec.issue_date, rec.expiry_date, rec.previous_id);
            END LOOP;
    END;
$$;