alter table telemechanic.ewb
    drop column if exists communication_type;

drop type if exists telemechanic.ewb_communication_type;