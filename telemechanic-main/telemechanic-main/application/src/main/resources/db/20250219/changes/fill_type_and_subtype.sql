DO
$$
    DECLARE
        rec record;
    BEGIN
        FOR rec IN
            select tr.state_number as state_number, st.title as subtype_title, t.title as type_title
            from vehicle.transport tr
            join vehicle.subtype st on tr.subtype_id = st.id
            join vehicle.type t on st.type_id = t.id
            LOOP
                update telemechanic.transport tr
                set subtype = rec.subtype_title, type = rec.type_title
                where tr.state_number = rec.state_number;
            END LOOP;
    END;
$$;