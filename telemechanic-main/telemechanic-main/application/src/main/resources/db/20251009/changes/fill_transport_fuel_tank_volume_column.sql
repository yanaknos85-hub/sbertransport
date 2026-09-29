DO
$do$
    BEGIN
        IF EXISTS(
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = 'vehicle'
                  and table_name = 'transport'
            ) THEN

            UPDATE telemechanic.transport tt
            SET fuel_tank_volume = vv.fuel_tank_volume
            FROM vehicle.transport vt
            JOIN vehicle.vehicle vv on vt.vehicle_id = vv.id
            WHERE tt.status = 'IN_USE' AND tt.id = vt.id;

            ALTER TABLE telemechanic.transport ALTER COLUMN fuel_tank_volume DROP DEFAULT;
        END IF;
    END
$do$;