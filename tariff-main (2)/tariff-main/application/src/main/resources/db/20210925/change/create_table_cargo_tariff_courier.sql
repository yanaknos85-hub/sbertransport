create table tariff.cargo_tariff_courier
(
    id uuid not null,
    tariff_id uuid not null
        constraint cargo_tariff_id_fkey
            references tariff.tariff,
    zone varchar(20),
    interval_id uuid not null
        constraint cargo_interval_id_fkey
            references tariff.cargo_intervals,
    express boolean not null default false,
    value float8
);

comment on table tariff.cargo_tariff_courier is 'Грузы. Тарифы курьерской доставки';
comment on column tariff.cargo_tariff_courier.interval_id is 'Ссылка на интерал';
comment on column tariff.cargo_tariff_courier.express is 'Экспресс / не экспресс';
comment on column tariff.cargo_tariff_courier.value is 'Значеник тарифа';
comment on column tariff.cargo_tariff_courier.zone is 'Зона тарификации';