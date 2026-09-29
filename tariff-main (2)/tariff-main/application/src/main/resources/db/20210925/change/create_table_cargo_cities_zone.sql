create table tariff.cargo_cities_zone
(
    id         uuid not null primary key,
    tariff_id uuid not null,
    zone       varchar(20),
    point_from uuid not null
        constraint cargo_cities_id_fkey1
            references tariff.cargo_cities,
    point_to   uuid not null
        constraint cargo_cities_id_fkey2
            references tariff.cargo_cities
);

comment on table tariff.cargo_cities_zone is 'Грузы. Зоны для тарификации курьерской доставки';
comment on column tariff.cargo_cities_zone.tariff_id is 'Ссылка на тариф';
comment on column tariff.cargo_cities_zone.zone is 'Зона';
comment on column tariff.cargo_cities_zone.point_from is 'Город отправления груза';
comment on column tariff.cargo_cities_zone.point_to is 'Город доставки груза';