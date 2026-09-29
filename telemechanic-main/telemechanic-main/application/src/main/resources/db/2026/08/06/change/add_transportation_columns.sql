alter table telemechanic.ewb
    add column transportation_type_id uuid
        references telemechanic.transportation_type (id);

alter table telemechanic.ewb
    add column transportation_subtype_id uuid
        references telemechanic.transportation_subtype (id);