alter table vehicle.model
    drop constraint model_title_uc;

create unique index model_title_brand_id_uc
    on vehicle.model (title, brand_id);

alter table vehicle.wheel_size
    alter column title type varchar(50);

drop index vehicle.vehicle_vehicle_valuable_fields_idx;

alter table vehicle.vehicle
    add body_type_id uuid references vehicle.body_type (id) not null;
alter table vehicle.vehicle
    add transmission_type_id uuid references vehicle.transmission_type (id) not null;
alter table vehicle.vehicle
    add front_wheel_size_id uuid references vehicle.wheel_size (id) not null;
alter table vehicle.vehicle
    add rear_wheel_size_id uuid references vehicle.wheel_size (id) not null;
COMMENT ON COLUMN vehicle.vehicle.body_type_id is 'Тип кузова';
COMMENT ON COLUMN vehicle.vehicle.transmission_type_id is 'Тип трансмиссии';
COMMENT ON COLUMN vehicle.vehicle.front_wheel_size_id is 'Размер переднего колеса';
COMMENT ON COLUMN vehicle.vehicle.rear_wheel_size_id is 'Размер заднего колеса';
COMMENT ON COLUMN vehicle.vehicle.drive_id is 'ID Привод';
comment on column vehicle.vehicle.service_interval_days is 'Межсервисный интервал по времени';
comment on column vehicle.vehicle.service_authorization_days is 'Допуск по времени';
comment on column vehicle.vehicle.service_interval_mileage is 'Межсервисный интервал по времени';
comment on column vehicle.vehicle.service_authorization_mileage is 'Допуск по пробегу';

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
                        rear_wheel_size_id);