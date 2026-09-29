alter table contractors.vehicle add column active boolean default true not null;

COMMENT ON COLUMN contractors.vehicle.active IS 'Активен';