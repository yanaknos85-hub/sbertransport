alter table tariff.tariff
    add column region_ids uuid[];
comment on column tariff.tariff.region_ids is 'Список регионов';
alter table tariff.tariff
    add column transport_ids uuid[];
comment on column tariff.tariff.transport_ids is 'Список транспорта';