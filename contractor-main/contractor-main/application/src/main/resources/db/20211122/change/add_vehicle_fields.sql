alter table contractors.vehicle add column if not exists engine_type varchar(255);
alter table contractors.vehicle add column if not exists in_exploitation boolean;


COMMENT ON COLUMN contractors.vehicle.engine_type IS 'Тип двигателя';
COMMENT ON COLUMN contractors.vehicle.in_exploitation IS 'В эксплуатации';