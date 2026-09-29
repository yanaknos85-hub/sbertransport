create table if not exists telemechanic.transportation_type (
    id    uuid         primary key,
    title varchar(255) not null
);

create table if not exists telemechanic.transportation_subtype (
    id    uuid         primary key,
    title varchar(255) not null
);

alter table telemechanic.ewb
    add column if not exists transportation_type_id uuid;

alter table telemechanic.ewb
    add column if not exists transportation_subtype_id uuid;