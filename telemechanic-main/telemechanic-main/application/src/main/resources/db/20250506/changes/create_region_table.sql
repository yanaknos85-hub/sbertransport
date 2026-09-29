create table if not exists telemechanic.region(
    id uuid primary key,
    code varchar(3) not null unique,
    name varchar(255) not null
);

comment on table telemechanic.region is 'Справочник регионов Российской Федерации';
comment on column telemechanic.region.id is 'Идентификатор записи о регионе';
comment on column telemechanic.region.code is 'Код региона';
comment on column telemechanic.region.name is 'Наименование региона';