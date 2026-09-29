alter table vehicle.transport add column if not exists accessible_position_id UUID constraint fk_transport_accessible_position_id REFERENCES vehicle.accessible_position(id);

CREATE INDEX if NOT EXISTS idx_transport_accessible_position ON vehicle.transport(accessible_position_id);

comment on column vehicle.transport.accessible_position_id is 'Должность для закрепления ТС';


