ALTER TABLE reports."tariff"
ADD COLUMN region_id UUID;

COMMENT ON COLUMN reports."tariff".region_id IS 'ID региона';