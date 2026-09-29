create type telemechanic.ewb_communication_type as enum ('URBAN', 'SUBURBAN', 'INTERCITY');

alter table telemechanic.ewb
    add column communication_type telemechanic.ewb_communication_type;

comment on column telemechanic.ewb.communication_type is 'Вид сообщения';