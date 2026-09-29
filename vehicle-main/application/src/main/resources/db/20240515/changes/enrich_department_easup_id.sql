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
            update vehicle.department set easup_id = corporate.department.easup_id
            from corporate.department
            where vehicle.department.id = corporate.department.id
              and vehicle.department.easup_id is null ;
        END IF;
    END
$$;