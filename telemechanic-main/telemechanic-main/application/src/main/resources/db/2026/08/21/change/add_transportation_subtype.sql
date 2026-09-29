create type telemechanic.ewb_transportation_subtype as enum (
    'REGULAR_PASSENGER_TRANSPORTATION',
    'CARGO_TRANSPORTATION',
    'ON_DEMAND_PASSENGER_TRANSPORTATION',
    'PASSENGER_TAXI_TRANSPORTATION',
    'BUS_TRANSPORTATION_OF_GROUPS_OF_CHILDREN'
);

alter table telemechanic.ewb
    add column transportation_subtype telemechanic.ewb_transportation_subtype;

comment on column telemechanic.ewb.transportation_subtype is 'Подвид сообщения';