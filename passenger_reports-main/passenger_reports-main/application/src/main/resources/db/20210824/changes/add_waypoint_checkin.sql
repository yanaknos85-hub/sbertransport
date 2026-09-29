ALTER TABLE reports.waypoint
    ADD COLUMN IF NOT EXISTS checkin_automatic boolean DEFAULT false,
    ADD COLUMN IF NOT EXISTS checkin_manual boolean DEFAULT false;

UPDATE reports.waypoint SET checkin_automatic = false;
UPDATE reports.waypoint SET checkin_manual = false;

COMMENT ON COLUMN reports.waypoint.checkin_automatic is 'Статус автоматического чекина';
COMMENT ON COLUMN reports.waypoint.checkin_manual is 'Статус ручного чекина';