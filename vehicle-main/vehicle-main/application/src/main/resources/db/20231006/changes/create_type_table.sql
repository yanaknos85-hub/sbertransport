CREATE TABLE vehicle.type
(
    id    uuid PRIMARY KEY,
    title VARCHAR(255)
        CONSTRAINT type_title_uc UNIQUE NOT NULL
);

comment on column vehicle.type.id is 'Идентификатор типа ТС';
comment on column vehicle.type.title is 'Наименование типа ТС';