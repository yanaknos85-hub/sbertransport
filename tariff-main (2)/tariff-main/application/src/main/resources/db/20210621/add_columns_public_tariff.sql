alter table tariff.tariff
    add column city_local_train_cost                        int4 default 0 not null,
    add column city_local_train_availability                boolean default false not null,
    add column travel_card_bus_cost                         int4 default 0 not null,
    add column travel_card_bus_availability                 boolean default false not null,
    add column travel_card_local_train_cost                 int4 default 0 not null,
    add column travel_card_local_train_availability         boolean default false not null,
    add column travel_card_trolleybus_cost                  int4 default 0 not null,
    add column travel_card_trolleybus_availability          boolean default false not null,
    add column travel_card_tram_cost                        int4 default 0 not null,
    add column travel_card_tram_availability                boolean default false not null,
    add column travel_card_metro_cost                       int4 default 0 not null,
    add column travel_card_metro_availability               boolean default false not null,
    add column travel_card_all_city_transport_cost          int4 default 0 not null,
    add column travel_card_all_city_transport_availability  boolean default false not null;

comment on column tariff.tariff.city_local_train_cost is 'Цена билета на электричку, коп';
comment on column tariff.tariff.city_local_train_availability is 'Доступность электрички в регионе';
comment on column tariff.tariff.travel_card_bus_cost is 'Стоимость проездного(мес) на автобус, коп';
comment on column tariff.tariff.travel_card_bus_availability is 'Доступность проездного на автобус';
comment on column tariff.tariff.travel_card_local_train_cost is 'Стоимость проездного(мес) на электричку, коп';
comment on column tariff.tariff.travel_card_local_train_availability is 'Доступность проездного на электричку';
comment on column tariff.tariff.travel_card_trolleybus_cost is 'Стоимость проездного(мес) на троллейбус, коп';
comment on column tariff.tariff.travel_card_trolleybus_availability is 'Доступность проездного на троллейбус';
comment on column tariff.tariff.travel_card_tram_cost is 'Стоимость проездного(мес) на трамвай, коп';
comment on column tariff.tariff.travel_card_tram_availability is 'Доступность проездного на трамвай';
comment on column tariff.tariff.travel_card_metro_cost is 'Стоимость проездного(мес) на метро';
comment on column tariff.tariff.travel_card_metro_availability is 'Доступность проездного на метро';
comment on column tariff.tariff.travel_card_all_city_transport_cost is 'Стоимость единого проездного(мес), коп';
comment on column tariff.tariff.travel_card_all_city_transport_availability is 'Доступность единого проездного';