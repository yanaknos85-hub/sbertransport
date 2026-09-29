INSERT INTO vehicle.vehicle_fuel_type (vehicle_id, fuel_type_id)
SELECT DISTINCT v.id AS vehicle_id, ft.id AS fuel_type_id
FROM vehicle.vehicle v
JOIN vehicle.engine_type et ON v.engine_type_id = et.id
JOIN vehicle.fuel_type ft ON ft.engine_type_id = et.id
WHERE NOT EXISTS (
    SELECT 1
    FROM vehicle.vehicle_fuel_type vft
    WHERE vft.vehicle_id = v.id AND vft.fuel_type_id = ft.id
);