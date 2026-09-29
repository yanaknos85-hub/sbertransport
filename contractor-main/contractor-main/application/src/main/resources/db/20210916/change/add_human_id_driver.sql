ALTER TABLE contractors.driver
    ADD COLUMN IF NOT EXISTS humanreadableid      VARCHAR(128);

COMMENT ON COLUMN CONTRACTORS.DRIVER.humanreadableid IS 'Водитель активен';
