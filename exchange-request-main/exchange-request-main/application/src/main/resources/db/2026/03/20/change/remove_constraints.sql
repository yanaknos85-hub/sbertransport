ALTER TABLE exchange_request.special_conditions
DROP CONSTRAINT IF EXISTS chk_oversized_requires_flag;

ALTER TABLE exchange_request.vehicle_requirements
DROP CONSTRAINT IF EXISTS vehicle_requirements_load_capacity_check;

ALTER TABLE exchange_request.waypoint
ALTER COLUMN radius DROP NOT NULL;