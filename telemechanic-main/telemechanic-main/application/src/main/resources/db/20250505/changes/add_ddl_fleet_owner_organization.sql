create table if not exists telemechanic.fleet_owner_organization
(
    organization_id uuid not null
        constraint fleet_owner_organization_organization_id_fk
            references telemechanic.organization,
    active          boolean not null
);

comment on table telemechanic.fleet_owner_organization is 'Организация владельца автопарка';

comment on column telemechanic.fleet_owner_organization.organization_id is 'Идентификатор записи об организации';

comment on column telemechanic.fleet_owner_organization.active is 'Флаг активности';

create unique index if not exists fleet_owner_organization_organization_id_uindex
    on telemechanic.fleet_owner_organization (organization_id);