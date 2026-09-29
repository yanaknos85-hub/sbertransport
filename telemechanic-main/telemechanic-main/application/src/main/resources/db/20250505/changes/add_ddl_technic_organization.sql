create table if not exists telemechanic.technic_organization
(
    organization_id uuid not null
        constraint technic_organization_organization_id_fk
            references telemechanic.organization,
    active          boolean not null
);

comment on table telemechanic.technic_organization is 'Организация, осуществляющая технический осмотр';

comment on column telemechanic.technic_organization.organization_id is 'Идентификатор записи об организации';

comment on column telemechanic.technic_organization.active is 'Флаг активности';

create unique index if not exists technic_organization_organization_id_uindex
    on telemechanic.technic_organization (organization_id);