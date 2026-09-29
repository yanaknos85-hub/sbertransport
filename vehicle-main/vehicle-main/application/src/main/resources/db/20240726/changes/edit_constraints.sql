alter table vehicle.fuel_consumption
    drop constraint fuel_consumption_employee_id_fk;

alter table vehicle.fuel_consumption
    add constraint fuel_consumption_employee_user_id_fk
        foreign key (creator_user_id)
            references vehicle.employee (user_id);

alter table vehicle.odometer_value
    drop constraint odometer_value_employee_id_fk;

alter table vehicle.odometer_value
    add constraint odometer_value_employee_user_id_fk
        foreign key (creator_user_id)
            references vehicle.employee (user_id);