create table if not exists telemechanic.contact
(
    id    uuid not null
        constraint contact_pkey
            primary key,
    type  text not null,
    value text not null
);

comment on table telemechanic.contact is 'Контактные данные';

comment on column telemechanic.contact.id is 'Идентификатор записи о контактных данных';

comment on column telemechanic.contact.type is 'Тип (PHONE|EMAIL|SITE)';

comment on column telemechanic.contact.value is 'Значение';

create table if not exists telemechanic.organization_contact
(
    organization_id uuid not null
        constraint organization_contacts_organization_fkey
            references telemechanic.organization,
    contact_id      uuid not null
        constraint organization_contacts_contact_fkey
            references telemechanic.contact,
    constraint corporate_organization_contacts_contact_organization_uk
        unique (organization_id, contact_id)
);

comment on table telemechanic.organization_contact is 'Связь контактных данных и организаций';

comment on column telemechanic.organization_contact.organization_id is 'Идентификатор записи об организации';

comment on column telemechanic.organization_contact.contact_id is 'Идентификатор записи о контактных данных';

