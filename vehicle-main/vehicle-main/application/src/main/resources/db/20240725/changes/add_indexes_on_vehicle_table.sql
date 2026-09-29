create index concurrently vehicle_body_type_id_idx
    on vehicle.vehicle using hash(body_type_id);

create index concurrently vehicle_fuel_type_id_idx
    on vehicle.vehicle using hash(fuel_type_id);

create index concurrently vehicle_transmission_type_id_idx
    on vehicle.vehicle using hash(transmission_type_id);

create index concurrently vehicle_fuel_tank_volume_idx
    on vehicle.vehicle(fuel_tank_volume);

create index concurrently vehicle_engine_power_idx
    on vehicle.vehicle(engine_power);

create index concurrently vehicle_year_manufacture_begin_idx
    on vehicle.vehicle(year_manufacture_begin);

create index concurrently vehicle_year_manufacture_end_idx
    on vehicle.vehicle(year_manufacture_end);

create index concurrently vehicle_manufacture_period_text_idx
    on vehicle.vehicle using hash ((year_manufacture_begin::text || ' - ' || coalesce(year_manufacture_end::text, 'н.в')));

drop index vehicle.vehicle_model_idx;
drop index vehicle.model_brand_idx;

create index concurrently vehicle_model_idx
    on vehicle.vehicle using hash(model_id);

create index model_brand_idx
    on vehicle.model using hash(brand_id);