create table if not exists telemechanic.category
(
    id          uuid
        constraint category_pkey primary key,
    category_code    varchar(5)      not null,
    title       varchar(255)    not null
);

comment on table telemechanic.category is 'Категория водительского удостоверения';
comment on column telemechanic.category.id is 'Идентификатор категории водительского удостоверения';
comment on column telemechanic.category.category_code is 'Категория водительского удостоверения';
comment on column telemechanic.category.title is 'Наименование категории водительского удостоверения';