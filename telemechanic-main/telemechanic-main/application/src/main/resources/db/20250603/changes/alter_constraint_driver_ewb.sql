alter table telemechanic.ewb
    drop constraint ewb_employee_id_fk;

alter table telemechanic.ewb
    add constraint ewb_drvier_id_fk
        foreign key (driver_id) references telemechanic.driver(id);