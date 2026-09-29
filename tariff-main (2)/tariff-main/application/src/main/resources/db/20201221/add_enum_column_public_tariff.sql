alter table tariff.tariff
    add column compensation_type  varchar(255)   not null   default 'CITY_TRIP_COMPENSATION';

comment on column tariff.tariff.compensation_type is 'Тип компенсации';