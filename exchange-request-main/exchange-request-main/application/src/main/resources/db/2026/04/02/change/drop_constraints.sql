-- устанавливаем DEFAULT = false

ALTER TABLE exchange_request.vehicle_requirements
ALTER COLUMN no_additional_load SET DEFAULT false;