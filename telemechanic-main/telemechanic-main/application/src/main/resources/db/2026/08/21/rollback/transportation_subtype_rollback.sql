alter table telemechanic.ewb
    drop column if exists transportation_subtype;

drop type if exists telemechanic.ewb_transportation_subtype;