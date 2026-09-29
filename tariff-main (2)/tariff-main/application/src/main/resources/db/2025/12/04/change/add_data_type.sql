create type tariff.class_type as enum (
    'ECONOMY',
    'COMFORT',
    'COMFORT_PLUS',
    'BUSINESS',
    'OFFICIAL',
    'VIP_BUS',
    'SMALL_BUS',
    'MIDDLE_BUS',
    'LARGE_BUS',
    'TRANSFER',
    'TRANSFER_COMFORT',
    'TRANSFER_COMFORT_PLUS',
    'TRANSFER_BUSINESS',
    'TRANSFER_VIP',
    'TRANSFER_CAR_CHOICE'
);

alter table tariff.tariff alter column taxi_class type tariff.class_type using taxi_class::tariff.class_type;
create index tariff_taxi_class_idx ON tariff.tariff using hash(taxi_class);