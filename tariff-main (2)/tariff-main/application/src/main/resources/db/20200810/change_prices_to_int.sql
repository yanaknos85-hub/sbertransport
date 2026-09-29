update tariff.tariff
set ride_cost_per_km = ride_cost_per_km * 100
where ride_cost_per_km is not null;
update tariff.tariff
set min_ride_cost = min_ride_cost * 100
where min_ride_cost is not null;
update tariff.tariff
set wait_cost_per_min = wait_cost_per_min * 100
where wait_cost_per_min is not null;
update tariff.tariff
set ride_cost_per_min = ride_cost_per_min * 100
where ride_cost_per_min is not null;
update tariff.tariff
set car_service_cost = car_service_cost * 100
where car_service_cost is not null;

alter table tariff.tariff
    alter column ride_cost_per_km set data type int4,
    alter column min_ride_cost set data type int4;



