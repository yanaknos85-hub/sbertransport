create table if not exists vehicle.driving_license_category
(
    driving_license_id uuid not null
        constraint driving_license_id_fk references vehicle.driving_license (id),
    category_id        uuid not null
        constraint category_id_fk references vehicle.category (id),
    constraint pk_driving_license_category primary key (driving_license_id, category_id)
);

comment on table vehicle.driving_license_category is 'Справочная таблица с категориями водительских прав';
comment on column vehicle.driving_license_category.driving_license_id is 'Идентификатор водительских прав';
comment on column vehicle.driving_license_category.category_id is 'Идентификатор категории';