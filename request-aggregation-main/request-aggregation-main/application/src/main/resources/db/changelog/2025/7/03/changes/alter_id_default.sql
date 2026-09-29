-- Установка автогенерации UUID для id в status
ALTER TABLE request_aggregation.status
    ALTER COLUMN id SET DEFAULT gen_random_uuid();

-- Установка автогенерации UUID для id в trip_type
ALTER TABLE request_aggregation.trip_type
    ALTER COLUMN id SET DEFAULT gen_random_uuid();

-- Установка автогенерации UUID для id в transport_type
ALTER TABLE request_aggregation.transport_type
    ALTER COLUMN id SET DEFAULT gen_random_uuid();