create table tariff.cargo_tariff_domestic_courier
(
    id uuid primary key,
    tariff_id uuid not null
        constraint cargo_tariff_dom_id_fkey
            references tariff.tariff,
    tb           varchar(20) not null,
    gosb         varchar(20) not null,
    point_from uuid not null
        constraint cargo_tariff_dom_id_fkey1
            references tariff.cargo_cities,
    point_to uuid not null
        constraint cargo_tariff_dom_id_fkey2
            references tariff.cargo_cities,
    max_day       int,
    value      float8
);

comment on table tariff.cargo_tariff_domestic_courier is 'Грузы. Тарифы внутреннего курьера';
comment on column tariff.cargo_tariff_domestic_courier.tariff_id is 'Ссылка на тариф';
comment on column tariff.cargo_tariff_domestic_courier.tb is 'Территориальный банк';
comment on column tariff.cargo_tariff_domestic_courier.point_from is 'Город/регион отправки груза';
comment on column tariff.cargo_tariff_domestic_courier.point_to is 'Город/регион прибытия груза';
comment on column tariff.cargo_tariff_domestic_courier.value is 'Тариф';
comment on column tariff.cargo_tariff_domestic_courier.max_day is 'Cрок доставки, рабочих дней';
