DO
$do$
    BEGIN
        IF EXISTS(
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = 'vehicle'
                  and table_name = 'transport'
            ) THEN
            INSERT INTO telemechanic.transport (
                id,
                state_number,
                brand,
                model,
                status,
                mileage,
                type,
                subtype
            )
            SELECT
                v.id,
                v.state_number,
                v.brand_by_passport,
                v.model_by_passport,
                v.status,
                v.current_mileage,
                t.title AS type_title,
                s.title AS subtype_title
            FROM vehicle.transport v
            JOIN vehicle.subtype s ON v.subtype_id = s.id
            JOIN vehicle.type t ON s.type_id = t.id
            ON CONFLICT (id) DO UPDATE SET
                            state_number = EXCLUDED.state_number,
                            brand = EXCLUDED.brand,
                            model = EXCLUDED.model,
                            status = EXCLUDED.status,
                            mileage = EXCLUDED.mileage,
                            type = EXCLUDED.type,
                            subtype = EXCLUDED.subtype;
        END IF;
    END
$do$;