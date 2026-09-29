alter table tariff.tariff
    drop column distance_included,
    add column distance_included              float8   not null default 0;
comment on column tariff.tariff.distance_included is 'Бесплатных километров пути, включенных в тариф';
