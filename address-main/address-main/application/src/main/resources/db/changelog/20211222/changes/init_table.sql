create schema addresses;

create table addresses.frequently
(
    id        uuid not null
        constraint frequently_addres_fkey
            primary key,
    country   text,
    region    text,
    city      text,
    street    text,
    house     text,
    building  text,
    structure text,
    latitude  numeric,
    longitude numeric,
    owner     uuid,
    count     numeric
);

comment on table addresses.frequently is 'Таблица с частыми адресами';

comment on column addresses.frequently.id is 'Идентификатор';

comment on column addresses.frequently.country is 'Страна';

comment on column addresses.frequently.region is 'Регион';

comment on column addresses.frequently.city is 'Город';

comment on column addresses.frequently.street is 'Улица';

comment on column addresses.frequently.house is 'Дом';

comment on column addresses.frequently.building is 'Корпус';

comment on column addresses.frequently.structure is 'Строение';

comment on column addresses.frequently.latitude is 'Широта';

comment on column addresses.frequently.longitude is 'Долгота';

comment on column addresses.frequently.owner is 'Владелец';

comment on column addresses.frequently.count is 'Количество использований';

create table addresses.favorite
(
    id        uuid not null
        constraint favorite_addres_fkey
            primary key,
    country   text,
    region    text,
    city      text,
    street    text,
    house     text,
    building  text,
    structure text,
    latitude  numeric,
    longitude numeric,
    owner     uuid,
    label     text
);

comment on table addresses.favorite is 'Таблица с любимыми адресами';

comment on column addresses.favorite.id is 'Идентификатор';

comment on column addresses.favorite.country is 'Страна';

comment on column addresses.favorite.region is 'Регион';

comment on column addresses.favorite.city is 'Город';

comment on column addresses.favorite.street is 'Улица';

comment on column addresses.favorite.house is 'Дом';

comment on column addresses.favorite.building is 'Корпус';

comment on column addresses.favorite.structure is 'Строение';

comment on column addresses.favorite.latitude is 'Широта';

comment on column addresses.favorite.longitude is 'Долгота';

comment on column addresses.favorite.owner is 'Владелец';

comment on column addresses.favorite.label is 'Метка';

create table addresses.urls
(
    id      uuid not null
        constraint users_urls_pkey
            primary key,
    url     text not null,
    pattern text not null,
    method  text not null
);

create table addresses.roles
(
    role   text not null,
    url_id uuid not null
        constraint role_urls_fkey
            references addresses.urls,
    constraint user_roles_ukey
        unique (url_id, role)
);