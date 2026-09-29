CREATE TABLE vehicle.vehicle_fuel_type (
    vehicle_id UUID NOT NULL,
    fuel_type_id UUID NOT NULL,
    PRIMARY KEY (vehicle_id, fuel_type_id),
    CONSTRAINT fk_vehicle_fuel_type_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle.vehicle(id),
    CONSTRAINT fk_vehicle_fuel_type_fuel_type FOREIGN KEY (fuel_type_id) REFERENCES vehicle.fuel_type(id)
);

COMMENT ON TABLE vehicle.vehicle_fuel_type IS 'Таблица связи между транспортом и типом топлива (многие ко многим)';
COMMENT ON COLUMN vehicle.vehicle_fuel_type.vehicle_id IS 'Идентификатор транспорта';
COMMENT ON COLUMN vehicle.vehicle_fuel_type.fuel_type_id IS 'Идентификатор типа топлива';

CREATE INDEX idx_vehicle_fuel_type_vehicle_id ON vehicle.vehicle_fuel_type(vehicle_id);