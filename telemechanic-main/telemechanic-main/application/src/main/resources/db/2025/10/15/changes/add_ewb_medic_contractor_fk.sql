alter table telemechanic.ewb
    add column medic_contractor_id uuid;

comment on column telemechanic.ewb.medic_contractor_id is 'Идентификатор медика сторонней организации';

alter table telemechanic.ewb
    add constraint ewb_medic_contractor_id_fk
        foreign key (medic_contractor_id) references telemechanic.medic_contractor;


alter table telemechanic.ewb
    add constraint ewb_medic_check_fields
        check (
            (medic_id is null and medic_contractor_id is null) or
            (medic_id is not null and medic_contractor_id is null) or
            (medic_id is null and medic_contractor_id is not null)
        );