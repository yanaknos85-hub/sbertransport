ALTER TABLE vehicle.vehicle
ADD COLUMN engine_type_id UUID;

COMMENT ON COLUMN vehicle.vehicle.engine_type_id IS 'Идентификатор типа двигателя';