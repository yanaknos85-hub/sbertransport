ALTER TABLE reports.waypoint
    DROP COLUMN IF EXISTS checkin_automatic,
    DROP COLUMN IF EXISTS checkin_manual;