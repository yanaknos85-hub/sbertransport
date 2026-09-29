create table if not exists telemechanic.transportation_subtype (
    id                     uuid         primary key,
    title                  varchar(255) not null,
    transportation_type_id uuid         not null references telemechanic.transportation_type(id)
);

comment on table telemechanic.transportation_subtype is 'Справочник подвидов перевозки';
comment on column telemechanic.transportation_subtype.id is 'Индентификатор записи';
comment on column telemechanic.transportation_subtype.title is 'Наименование подвида перевозки';
comment on column telemechanic.transportation_subtype.transportation_type_id is 'Идентификатор вида перевозки';