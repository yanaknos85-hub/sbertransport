alter table telemechanic.ewb
    add constraint ewb_vehicle_id_fk
        foreign key (transport_id) references telemechanic.vehicle;

alter table telemechanic.ewb
    add constraint ewb_employee_id_fk
        foreign key (driver_id) references telemechanic.employee;