alter table telemechanic.medic_request
    add organization_id uuid;

comment on column telemechanic.medic_request.organization_id is 'Идентификатор записи об организации';

create index medic_request_organization_id_index
    on telemechanic.medic_request (organization_id);

alter table telemechanic.medic_request
    add constraint medic_request_organization_id_fk
        foreign key (organization_id) references telemechanic.organization;