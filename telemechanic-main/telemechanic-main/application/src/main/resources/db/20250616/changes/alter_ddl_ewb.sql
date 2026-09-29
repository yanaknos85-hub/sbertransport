alter table telemechanic.ewb
    add organization_medical_license_id uuid;

comment on column telemechanic.ewb.organization_medical_license_id is 'Идентификатор записи о медицинской лицензии организации';

create index ewb_organization_medical_license_id_index
    on telemechanic.ewb (organization_medical_license_id);

alter table telemechanic.ewb
    add constraint ewb_organization_medical_license_id_fk
        foreign key (organization_medical_license_id) references telemechanic.organization_medical_license;

alter table telemechanic.ewb
    drop constraint fk_ewb_medic_license_id;

alter table telemechanic.ewb
    drop column medic_license_id;