alter table if exists telemechanic.ewb
    add column if not exists medic_license_id uuid;

alter table if exists telemechanic.ewb
    add constraint fk_ewb_medic_license_id foreign key (medic_license_id) references telemechanic.medical_license(id);

comment on column telemechanic.ewb.medic_license_id is 'Идентификатор медицинской лицензии';