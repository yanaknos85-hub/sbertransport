alter table telemechanic.organization
    add msrn text;

comment on column telemechanic.organization.msrn is 'ОГРН';

alter table telemechanic.organization
    add tin text;

comment on column telemechanic.organization.tin is 'ИНН';

alter table telemechanic.organization
    add organization_group_id uuid;

comment on column telemechanic.organization.organization_group_id is 'Идентификатор записи о группе организаций';

create index organization_organization_group_id_index
    on telemechanic.organization (organization_group_id);

alter table telemechanic.organization
    add constraint organization_organization_group_id_fk
        foreign key (organization_group_id) references telemechanic.organization_group (id);

