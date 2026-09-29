ALTER TABLE contractors.autopark ADD COLUMN contractor_id uuid
        constraint driver_contractor_fk REFERENCES CONTRACTORS.CONTRACTOR (ID);

COMMENT ON COLUMN contractors.autopark.contractor_id IS 'id связанного контрактора';