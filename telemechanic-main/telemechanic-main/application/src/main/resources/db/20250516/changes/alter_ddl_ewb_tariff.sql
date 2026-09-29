delete from telemechanic.ewb_tariff;

alter table telemechanic.ewb_tariff drop constraint ewb_tariff_organization_id_fk;

alter table telemechanic.ewb_tariff
    add constraint ewb_tariff_fleet_owner_organization_id_fk
        foreign key (organization_id) references telemechanic.fleet_owner_organization;

create index if not exists ewb_tariff_fleet_owner_organization_id_index
    on telemechanic.ewb_tariff (organization_id);