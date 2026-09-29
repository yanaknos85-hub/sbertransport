alter table tariff.tariff
    add column cost_loader integer;
alter table tariff.tariff drop column coast_loader;
comment on column tariff.tariff.cost_loader is 'Стоимость услуг грузчиков в точке маршрута';