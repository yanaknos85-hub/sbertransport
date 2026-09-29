alter table contractors.autopark add column active boolean default true not null;

COMMENT ON COLUMN contractors.autopark.active IS 'Активен';