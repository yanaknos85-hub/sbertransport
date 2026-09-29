create table tariff.cargo_intervals
(
    id         uuid   not null  primary key,
    cargo_from float8 not null,
    cargo_to   float8,
    step       float8
);

comment on table tariff.cargo_intervals is 'Грузы. Интервалы весов груза';
comment on column tariff.cargo_intervals.cargo_from is 'От';
comment on column tariff.cargo_intervals.cargo_to is 'До';
comment on column tariff.cargo_intervals.step is 'Шаг внутри интервала';
