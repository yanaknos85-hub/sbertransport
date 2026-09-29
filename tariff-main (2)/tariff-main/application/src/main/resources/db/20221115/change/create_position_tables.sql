create table tariff.message_position
(
    id              uuid                 not null
        constraint position_pkey
            primary key,
    position_name   varchar(255)         not null,
    self_approved   boolean,
    organization_id uuid                 not null,
    active          boolean default true not null
);

comment on column tariff.message_position.active is 'Флаг активности(неудаленности)';

create table tariff.message_position_taxi_classes
(
    position_id uuid not null
        constraint fk_position_taxi_class
            references tariff.message_position,
    taxi_class  varchar(255)
);