delete from telemechanic.fleet_owner_organization;

alter table telemechanic.fleet_owner_organization
    add constraint fleet_owner_organization_pk
        primary key (organization_id);

alter table telemechanic.fleet_owner_organization
    add edf_operator_id varchar(10) not null;

comment on column telemechanic.fleet_owner_organization.edf_operator_id is 'Идентификатор записи об операторе ЭДО';

alter table telemechanic.fleet_owner_organization
    add edf_code varchar(50) not null;

comment on column telemechanic.fleet_owner_organization.edf_code is 'Код участника';

