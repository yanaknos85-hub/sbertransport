ALTER TABLE vehicle.vehicle
ALTER COLUMN engine_type_id SET NOT NULL;

ALTER TABLE vehicle.vehicle
ADD CONSTRAINT fk_vehicle_engine_type_id FOREIGN KEY (engine_type_id) REFERENCES vehicle.engine_type(id);