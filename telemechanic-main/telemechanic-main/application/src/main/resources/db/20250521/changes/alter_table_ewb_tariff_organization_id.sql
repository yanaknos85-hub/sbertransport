alter table telemechanic.ewb
    add column tariff_department_id uuid;

alter table telemechanic.ewb
    add constraint fk_ewb_tariff_department_id
        foreign key (tariff_department_id)
            references telemechanic.department (id);