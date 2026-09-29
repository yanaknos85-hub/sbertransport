UPDATE vehicle.vehicle v
SET engine_type_id = ft.engine_type_id
FROM vehicle.fuel_type ft
WHERE v.fuel_type_id = ft.id;