alter table telemechanic.ewb
    drop column if exists transportation_type;

drop type if exists telemechanic.ewb_transportation_type;