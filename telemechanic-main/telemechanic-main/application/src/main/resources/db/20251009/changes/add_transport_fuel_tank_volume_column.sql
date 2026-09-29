alter table telemechanic.transport
    add column if not exists fuel_tank_volume int4 default 0 not null;

comment on column telemechanic.transport.fuel_tank_volume is 'Объем топливного бака';