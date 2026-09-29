create type telemechanic.ewb_transportation_type as enum (
    'COMMERCIAL_TRANSPORTATION',
    'OWN_ACCOUNT_TRANSPORTATION',
    'SPECIAL_PURPOSE_VEHICLES'
);

alter table telemechanic.ewb
    add column transportation_type telemechanic.ewb_transportation_type;

comment on column telemechanic.ewb.transportation_type is 'Вид перевозки';