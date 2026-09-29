alter table srm.tariff add column if not exists max_weight float8 default 0;
comment on column srm.tariff.max_weight is 'Максимальный вес';

alter table srm.tariff add column if not exists max_volume float8 default 0;
comment on column srm.tariff.max_volume is 'Максимальный объем';

alter table srm.tariff add column if not exists max_route_length float8 default 0;
comment on column srm.tariff.max_route_length is 'Максимальная протяженность маршрута';

alter table srm.tariff add column if not exists max_waypoint_count int4 default 0;
comment on column srm.tariff.max_waypoint_count is 'Максимальное количество точек маршрута';

alter table srm.tariff add column if not exists tariff_km int4 default 0;
comment on column srm.tariff.tariff_km is 'Грузовой тариф за поездку 1 км, руб.';

alter table srm.tariff add column if not exists cost_loader int4 default 0;
comment on column srm.tariff.cost_loader is 'Стоимость услуг грузчиков в точке маршрута';

alter table srm.tariff add column if not exists express int4 default 0;
comment on column srm.tariff.express is 'Доплата за Экспресс';

alter table srm.srm_request_kpi add column if not exists required_volume float8 default 0;
comment on column srm.srm_request_kpi.required_volume is 'Требуемый обьем груза';

alter table srm.srm_request_kpi add column if not exists required_weight float8 default 0;
comment on column srm.srm_request_kpi.required_weight is 'Требуемый вес груза';

alter table srm.srm_request_kpi add column if not exists active boolean default true;
comment on column srm.srm_request_kpi.active is 'Заявка активна';

alter table srm.srm_request_kpi add column if not exists cargo_express boolean default true;
comment on column srm.srm_request_kpi.cargo_express is 'Режим ЭКСПРЕСС';

alter table srm.srm_shared_ride add column if not exists bunch_id UUID;
comment on column srm.srm_shared_ride.bunch_id is 'ID связки';

alter table srm.srm_waypoint add column if not exists loader_number int4;
comment on column srm.srm_waypoint.loader_number is 'Количество грузчиков';