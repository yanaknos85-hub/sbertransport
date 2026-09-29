ALTER TABLE reports.request
DROP COLUMN deadline;

ALTER TABLE reports.request
ADD COLUMN deadline TIMESTAMP;