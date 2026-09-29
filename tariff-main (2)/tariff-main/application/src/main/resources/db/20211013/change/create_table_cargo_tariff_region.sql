create table tariff.cargo_tariff_region
(
    id uuid primary key,
    tariff_id uuid not null
        constraint cargo_tariff_id_fkey
            references tariff.tariff,
    tb           varchar(20) not null,
    point_from uuid not null
        constraint cargo_region_city_id_fkey1
            references tariff.cargo_cities,
    point_to uuid not null
        constraint cargo_region_city_id_fkey2
            references tariff.cargo_cities,
    interval_id uuid not null
        constraint cargo_region_interval_id_fkey
            references tariff.cargo_intervals,
    cargo_average float8,
    count_average int,
    max_day       int,
    value      float8,
    value_step float8
);

comment on table tariff.cargo_tariff_region is 'Грузы. Тарифы межрегиональной доставки';
comment on column tariff.cargo_tariff_region.tariff_id is 'Ссылка на тариф';
comment on column tariff.cargo_tariff_region.tb is 'Территориальный банк';
comment on column tariff.cargo_tariff_region.point_from is 'Город/регион отправки груза';
comment on column tariff.cargo_tariff_region.point_to is 'Город/регион прибытия груза';
comment on column tariff.cargo_tariff_region.interval_id is 'Ссылка на весовой интервал';
comment on column tariff.cargo_tariff_region.value is 'Тариф';
comment on column tariff.cargo_tariff_region.value_step is 'Тариф за 1 шаг (обычно за 1 кг)';
comment on column tariff.cargo_tariff_region.max_day is 'Максимальный срок доставки, рабочих дней';
comment on column tariff.cargo_tariff_region.cargo_average is 'Средний вес отправлений, кг';
comment on column tariff.cargo_tariff_region.count_average is 'Ориентировочное кол-во отправлений';