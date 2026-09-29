CREATE TABLE vehicle.body_type
(
    id    uuid PRIMARY KEY,
    title VARCHAR(255)
        CONSTRAINT body_type_title_uc UNIQUE NOT NULL
);
comment on table vehicle.body_type is 'Тип кузова ТС';
comment on column vehicle.body_type.id is 'Идентификатор Типа кузова ТС';
comment on column vehicle.body_type.title is 'Наименование Типа кузова ТС';