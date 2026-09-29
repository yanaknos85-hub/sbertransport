DO
$$
    DECLARE
        rec record;
    BEGIN
        FOR rec IN
            select c.*
            from vehicle.category c
            LOOP
                insert into telemechanic.category(id, category_code, title)
                values (rec.id, rec.category, rec.title);
            END LOOP;
    END;
$$;