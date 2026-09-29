alter table tariff.tariff
    add column tariff_km         float8,
    add column coast_loader      float8,
    add column express           float8;

comment on column tariff.tariff.tariff_km is 'Тариф за поездку 1 км, руб.';
comment on column tariff.tariff.coast_loader is 'Стоимость услуг грузчиков в точке маршрута';
comment on column tariff.tariff.express is 'Доплата за "Экспресс"';