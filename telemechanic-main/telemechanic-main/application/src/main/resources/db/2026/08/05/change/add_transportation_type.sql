create table if not exists telemechanic.transportation_type (
    id    uuid         primary key,
    title varchar(255) not null
);

comment on table telemechanic.transportation_type is 'Справочник видов перевозки';
comment on column telemechanic.transportation_type.id is 'Индентификатор записи';
comment on column telemechanic.transportation_type.title is 'Наименование вида перевозки';