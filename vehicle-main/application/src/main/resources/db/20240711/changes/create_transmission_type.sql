CREATE TABLE vehicle.transmission_type
(
    id    uuid PRIMARY KEY,
    title VARCHAR(50)
        CONSTRAINT transmission_type_title_uc UNIQUE NOT NULL
);
comment on table vehicle.transmission_type is 'Тип Трансмиссии ТС';
comment on column vehicle.transmission_type.id is 'Идентификатор Типа Трансмиссии ТС';
comment on column vehicle.transmission_type.title is 'Наименование Типа Трансмиссии ТС';