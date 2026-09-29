CREATE TABLE vehicle.subtype
(
    id    uuid PRIMARY KEY,
    title VARCHAR(255)
        CONSTRAINT subtype_title_uc UNIQUE NOT NULL,
    type_id uuid REFERENCES vehicle.type NOT NULL
);
comment on table vehicle.subtype is 'Подтип транспортного средства';
comment on column vehicle.subtype.id is 'ИД подптипа ТС';
comment on column vehicle.subtype.title is 'Наименование подтипа ТС';
comment on column vehicle.subtype.type_id is 'Тип подтипа ТС';