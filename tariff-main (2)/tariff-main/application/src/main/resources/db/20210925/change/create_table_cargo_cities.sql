create table tariff.cargo_cities
(
    id uuid primary key,
    name varchar(255) not null unique
);

comment on table tariff.cargo_cities is 'Грузы. Справочник городов/регионов для тарификации грузов';
comment on column tariff.cargo_cities.name is 'Название города';




