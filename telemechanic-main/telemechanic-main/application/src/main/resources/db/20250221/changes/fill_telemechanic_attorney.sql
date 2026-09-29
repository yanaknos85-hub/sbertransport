DO
$$
    DECLARE
        rec record;
    BEGIN
        FOR rec IN
            select
                a.id as id,
                a.telemechanic_id as telemechanic_id,
                a.attorney_id as attorney_id,
                a.issue_date as issue_date,
                a.expiry_date as expiry_date,
                a.creation_system as creation_system
            from vehicle.attorney a
            LOOP
                insert into telemechanic.attorney (id, telemechanic_id, attorney_id, issue_date, expiry_date, creation_system)
                values (rec.id, rec.telemechanic_id, rec.attorney_id, rec.issue_date, rec.expiry_date, rec.creation_system);
            END LOOP;
    END;
$$;