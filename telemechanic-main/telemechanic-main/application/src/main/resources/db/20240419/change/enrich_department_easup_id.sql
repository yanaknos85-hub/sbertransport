DO
$$
    BEGIN
        IF EXISTS(
                SELECT table_name
                FROM information_schema.tables
                WHERE table_catalog = 'transport'
                  and table_schema = 'corporate'
                  and table_name = 'department'
            ) THEN
            update telemechanic.department set easup_id = corporate.department.easup_id
            from corporate.department
            where telemechanic.department.id = corporate.department.id
              and telemechanic.department.easup_id is null ;
        END IF;
    END
$$;