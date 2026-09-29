alter table telemechanic.organization_address
    add constraint organization_address_organization_id_fk
        foreign key (organization_id) references telemechanic.organization (id);