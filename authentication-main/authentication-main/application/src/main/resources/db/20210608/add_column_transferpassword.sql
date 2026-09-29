ALTER TABLE authentication.account
    ADD COLUMN transfer_password BOOLEAN DEFAULT false;

COMMENT ON COLUMN authentication.account.transfer_password IS 'Транспортный пароль';