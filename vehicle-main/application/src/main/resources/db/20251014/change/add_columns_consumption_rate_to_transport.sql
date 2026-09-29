alter table vehicle.vehicle add column if not exists city_consumption_rate decimal(5,2);
alter table vehicle.vehicle add column if not exists country_consumption_rate decimal(5,2);
alter table vehicle.vehicle add column if not exists hybrid_consumption_rate decimal(5,2);

comment on column vehicle.vehicle.city_consumption_rate is 'Городской расход';
comment on column vehicle.vehicle.country_consumption_rate is 'Загородный расход';
comment on column vehicle.vehicle.hybrid_consumption_rate is 'Смешанный расход';


