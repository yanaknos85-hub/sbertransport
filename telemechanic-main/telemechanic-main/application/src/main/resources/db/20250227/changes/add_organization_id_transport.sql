alter table telemechanic.transport
    add column organization_id uuid;

alter table telemechanic.transport
    add constraint transport_organization_id_fk foreign key (organization_id) references telemechanic.organization(id);

comment on column telemechanic.transport.organization_id is 'Идентификатор организации';