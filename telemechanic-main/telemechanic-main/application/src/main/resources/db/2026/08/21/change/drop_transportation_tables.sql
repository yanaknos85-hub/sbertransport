alter table telemechanic.ewb
    drop column if exists transportation_type_id;

alter table telemechanic.ewb
    drop column if exists transportation_subtype_id;

drop table if exists telemechanic.transportation_subtype;
drop table if exists telemechanic.transportation_type;