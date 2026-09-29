ALTER TABLE exchange_request.cargo_details
ALTER COLUMN cargo_type TYPE JSONB USING to_jsonb(cargo_type),
ALTER COLUMN cargo_package TYPE JSONB USING to_jsonb(cargo_package);

ALTER TABLE exchange_request.vehicle_requirements
ALTER COLUMN vehicle_body_type TYPE JSONB USING to_jsonb(vehicle_body_type),
ALTER COLUMN vehicle_extra_features TYPE JSONB USING to_jsonb(vehicle_extra_features);

CREATE INDEX idx_vr_vehicle_body_type_gin ON exchange_request.vehicle_requirements USING GIN (vehicle_body_type);