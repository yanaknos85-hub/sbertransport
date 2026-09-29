alter table tariff.tariff
    alter column ride_cost_per_km drop not null,

    add column metro_ticket_cost              int4,
    add column tram_ticket_cost               int4,
    add column trolleybus_ticket_cost         int4,
    add column bus_ticket_cost                int4,
    add column metro_availability             boolean,
    add column tram_availability              boolean,
    add column trolleybus_availability        boolean,
    add column bus_availability               boolean;

comment on column tariff.tariff.metro_ticket_cost is 'Цена билета на метро, коп';
comment on column tariff.tariff.tram_ticket_cost is 'Цена билета на трамвай, коп';
comment on column tariff.tariff.trolleybus_ticket_cost is 'Цена билета на троллейбус, коп';
comment on column tariff.tariff.bus_ticket_cost is 'Цена билета на автобус, коп';
comment on column tariff.tariff.metro_availability is 'Доступность метро в регионе';
comment on column tariff.tariff.tram_availability is 'Доступность трамвая в регионе';
comment on column tariff.tariff.trolleybus_availability is 'Доступность троллейбуса в регионе';
comment on column tariff.tariff.bus_availability is 'Доступность автобуса в регионе';
