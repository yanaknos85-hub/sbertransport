create table if not exists telemechanic.organization_address(
    organization_id uuid        not null
        constraint organization_address_pkey
            primary key,
    zip             varchar(6)  not null,
    region_id       uuid        not null
        constraint organization_address_region_id_fk
            references telemechanic.region (id)
);

comment on table telemechanic.organization_address is 'Адрес организации';
comment on column telemechanic.organization_address.organization_id is 'Идентификатор организации';
comment on column telemechanic.organization_address.zip is 'Индекс';
comment on column telemechanic.organization_address.region_id is 'Идентификатор региона';