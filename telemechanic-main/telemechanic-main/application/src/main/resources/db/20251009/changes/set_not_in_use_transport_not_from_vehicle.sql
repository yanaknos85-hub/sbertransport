DO
$do$
    BEGIN
        IF EXISTS(
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = 'vehicle'
                  and table_name = 'transport'
            ) THEN
            UPDATE telemechanic.transport t1
            SET status = 'NOT_IN_USE'
            WHERE NOT EXISTS (SELECT 1 FROM vehicle.transport t2 WHERE t2.id = t1.id);
        END IF;
    END
$do$;
