CREATE TABLE vehicle.category
(
    id    uuid PRIMARY KEY,
    category VARCHAR (5) CONSTRAINT category_category_uc UNIQUE NOT NULL,
    title VARCHAR(255) CONSTRAINT category_title_uc UNIQUE NOT NULL
);

comment on column vehicle.category.id is 'Идентификатор записи о Категории ТС';
comment on column vehicle.category.category is 'Идентификатор Категории ТС';
comment on column vehicle.category.title is 'Наименование Категории ТС';