create table if not exists telemechanic.driving_license_category
(
    driving_license_id uuid not null,
    category_id uuid not null,
        constraint driving_license_category_pkey primary key (driving_license_id, category_id),
        constraint category_fk foreign key (category_id)
            references telemechanic.category (id),
        constraint driving_license_fk foreign key (driving_license_id)
            references telemechanic.driving_license (id)
);

comment on table telemechanic.driving_license_category is 'Справочная таблица с категориями водительских прав';
comment on column telemechanic.driving_license_category.driving_license_id is 'Идентификатор водительских прав';
comment on column telemechanic.driving_license_category.category_id is 'Идентификатор категории водительских прав';