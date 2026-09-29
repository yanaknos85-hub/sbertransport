alter table telemechanic.transport
    add column if not exists fuel_litreage int4;

comment on column telemechanic.transport.fuel_litreage is 'Остаток топлива';