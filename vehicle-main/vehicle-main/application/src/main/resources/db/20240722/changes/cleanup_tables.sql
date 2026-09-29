truncate table vehicle.brand cascade;
truncate table vehicle.engine_type cascade;
truncate table vehicle.drive cascade;
truncate table vehicle.wheel_size cascade;
truncate table vehicle.body_type cascade;
truncate table vehicle.transmission_type cascade;

comment on column vehicle.vehicle.service_interval_mileage is 'Межсервисный интервал по пробегу';