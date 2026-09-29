ALTER TABLE contractors.autopark
DROP CONSTRAINT driver_contractor_fk;

ALTER TABLE contractors.autopark DROP COLUMN contractor_id;