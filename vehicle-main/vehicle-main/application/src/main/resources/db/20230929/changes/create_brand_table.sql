CREATE TABLE vehicle.brand
(
    id    uuid PRIMARY KEY,
    title VARCHAR(255)
        CONSTRAINT brand_title_uc UNIQUE NOT NULL
);

comment on column vehicle.brand.id is 'Идентификатор марки ТС';
comment on column vehicle.brand.title is 'Наименование марки ТС';