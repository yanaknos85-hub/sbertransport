create table if not exists telemechanic.medic_organization
(
    organization_id uuid    not null
        constraint medic_organization_organization_id_fk
            references telemechanic.organization,
    active          boolean not null
);

comment on table telemechanic.medic_organization is 'Организация, осуществляющая медицинский осмотр';

comment on column telemechanic.medic_organization.organization_id is 'Идентификатор записи об организации';

comment on column telemechanic.medic_organization.active is 'Флаг активности';

create unique index if not exists medic_organization_organization_id_uindex
    on telemechanic.medic_organization (organization_id);

