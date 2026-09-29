ALTER TABLE exchange_request.waypoint
ADD COLUMN IF NOT EXISTS date DATE NOT NULL DEFAULT '2025-01-01',
ADD COLUMN IF NOT EXISTS from_time TIME WITHOUT TIME ZONE NOT NULL DEFAULT '00:00:00',
ADD COLUMN IF NOT EXISTS to_time TIME WITHOUT TIME ZONE NOT NULL DEFAULT '00:00:00';

UPDATE exchange_request.waypoint
   SET
       date = date_from::date,
       from_time = date_from::time,
       to_time = date_to::time;

ALTER TABLE exchange_request.waypoint
DROP COLUMN IF EXISTS date_from,
DROP COLUMN IF EXISTS date_to;