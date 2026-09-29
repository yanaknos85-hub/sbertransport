CREATE TABLE vehicle.engine_type
(
    id    uuid PRIMARY KEY,
    title VARCHAR(255)
        CONSTRAINT engine_type_title_uc UNIQUE NOT NULL
);

comment on column vehicle.engine_type.id is 'Идентификатор типа двигателя ТС';
comment on column vehicle.engine_type.title is 'Наименование типа двигателя ТС';