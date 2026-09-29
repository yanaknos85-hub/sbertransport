alter table telemechanic.ewb
    add column if not exists fuel_litreage_in int8;
alter table telemechanic.ewb
    add column if not exists fuel_litreage_out int8;

comment on column telemechanic.ewb.fuel_litreage_in is 'Остаток топлива  при возвращении в гараж';
comment on column telemechanic.ewb.fuel_litreage_out is 'Остаток топлива при выходе на линию';