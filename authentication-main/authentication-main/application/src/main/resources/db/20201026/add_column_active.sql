ALTER TABLE authentication.account
    ADD COLUMN active BOOLEAN DEFAULT true;

COMMENT ON COLUMN authentication.account.active IS 'Флаг активности уч. записи';