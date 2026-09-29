alter table telemechanic.request
    add organization_id uuid;

comment on column telemechanic.request.organization_id is 'Идентификатор записи об организации';

create index request_organization_id_index
    on telemechanic.request (organization_id);

alter table telemechanic.request
    add constraint request_organization_id_fk
        foreign key (organization_id) references telemechanic.organization;