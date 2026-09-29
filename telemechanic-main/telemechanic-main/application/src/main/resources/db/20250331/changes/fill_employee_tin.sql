DO
$$
    DECLARE
        rec record;
    BEGIN
        FOR rec IN
            select emp.id as employee_id, emp.tin as tin
            from vehicle.employee emp
            where emp.tin is not null
            LOOP
                insert into telemechanic.tin(id, employee_id, tin)
                values (md5(random()::text || clock_timestamp()::text)::uuid, rec.employee_id, rec.tin);
            END LOOP;
    END;
$$;