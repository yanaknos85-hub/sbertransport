DO
$$
    DECLARE
        rec record;
    BEGIN
        FOR rec IN
            select dlc.*
            from vehicle.driving_license_category dlc
            LOOP
                insert into telemechanic.driving_license_category(driving_license_id, category_id)
                values (rec.driving_license_id, rec.category_id);
            END LOOP;
    END;
$$;