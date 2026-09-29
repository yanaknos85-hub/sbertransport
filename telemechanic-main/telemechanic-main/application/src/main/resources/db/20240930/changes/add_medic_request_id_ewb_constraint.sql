alter table telemechanic.ewb
    add constraint ewb_medic_request_id_fk
        foreign key (medic_request_id) references telemechanic.medic_request;