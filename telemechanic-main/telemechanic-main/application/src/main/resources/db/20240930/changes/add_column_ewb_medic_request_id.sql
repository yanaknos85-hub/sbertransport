alter table telemechanic.ewb
    add medic_request_id uuid;

comment on column telemechanic.ewb.medic_request_id is 'ID заявки проверок медика';