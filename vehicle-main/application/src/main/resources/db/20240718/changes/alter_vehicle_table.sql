truncate table vehicle.vehicle cascade;
drop index vehicle.vehicle_vehicle_valuable_fields_idx;

alter table vehicle.vehicle
    add year_manufacture_begin int not null;
alter table vehicle.vehicle
    add year_manufacture_end int;
comment on column vehicle.vehicle.year_manufacture_begin is 'Год начала производства';
comment on column vehicle.vehicle.year_manufacture_end is 'Год снятия с производства';

create unique index vehicle_vehicle_valuable_fields_idx
    on vehicle.vehicle (model_id,
                        category_id,
                        manufacturer,
                        ecological_class,
                        engine_power,
                        engine_capacity,
                        fuel_tank_volume,
                        fuel_type_id,
                        drive_id,
                        mudguard_installed,
                        spare_wheel_holder_installed,
                        weight,
                        max_weight,
                        height,
                        width,
                        length,
                        service_interval_days,
                        service_interval_mileage,
                        service_authorization_days,
                        service_authorization_mileage,
                        body_type_id,
                        transmission_type_id,
                        front_wheel_size_id,
                        rear_wheel_size_id,
                        year_manufacture_begin,
                        coalesce(year_manufacture_end, 0));