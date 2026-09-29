ALTER TABLE vehicle.transport
    DROP COLUMN status_id;

ALTER TABLE vehicle.transport
    ADD status varchar(20) NOT NULL;

COMMENT ON COLUMN vehicle.transport.status IS 'Статус';

CREATE INDEX transport_state_number_idx
    ON vehicle.transport (UPPER(state_number));

CREATE INDEX transport_vin_code_idx
    ON vehicle.transport (UPPER(vin_code));

CREATE UNIQUE INDEX transport_vin_code_uindex
    ON vehicle.transport (UPPER(vin_code));

CREATE UNIQUE INDEX transport_asset_number_uindex
    ON vehicle.transport (UPPER(asset_number));

CREATE UNIQUE INDEX transport_state_number_active_uindex
    ON vehicle.transport (state_number, status)
    WHERE (status = 'IN_USE');