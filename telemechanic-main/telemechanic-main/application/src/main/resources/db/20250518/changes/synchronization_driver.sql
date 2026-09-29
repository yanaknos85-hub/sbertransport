DO
$$
    DECLARE
        rec record;
    BEGIN
        FOR rec IN
            SELECT dl.id, dl.driver_id, emp.tin
            FROM vehicle.driving_license dl
            JOIN vehicle.employee emp ON dl.driver_id = emp.id
            WHERE (dl.driver_id, dl.issue_date) IN (
            	SELECT dl2.driver_id, max(dl2.issue_date)
            	FROM vehicle.driving_license dl2
            	GROUP BY dl2.driver_id
            )
        LOOP
            INSERT INTO telemechanic.driver(id, employee_id, tin, snils, driving_license_id)
            VALUES (md5(random()::text || clock_timestamp()::text)::uuid, rec.driver_id, rec.tin, null, rec.id);
        END LOOP;
    END;
$$;