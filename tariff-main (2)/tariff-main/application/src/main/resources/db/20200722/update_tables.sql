alter table tariff.tariff
    add column if not exists organization_id       uuid,
    add column if not exists transport_type        varchar(50),
    add column if not exists coefficient           float8,
    add column if not exists reward_for_passenger  int4,
    add column if not exists season_start          date,
    add column if not exists season_end            date,
    add column if not exists car_service_cost      int4,
    add column if not exists dept_id               varchar(255),
    add column if not exists dept_name             varchar(255),
    add column if not exists dept_struct           varchar(255),
    add column if not exists distance_deviation_km float8,
    add column if not exists free_waiting_time     int4,
    add column if not exists max_capacity          int4,
    add column if not exists min_cancel_time_min   int4,
    add column if not exists min_ride_cost         float8,
    add column if not exists min_service_time      int4,
    add column if not exists ride_cost_per_min     int4,
    add column if not exists savings_deviation_pct float8,
    add column if not exists time_deviation_min    int4,
    add column if not exists toll_roads            boolean,
    add column if not exists historical_traffic    boolean,
    add column if not exists vehicle_type          varchar(255),
    add column if not exists taxi_class            varchar(255),
    add column if not exists wait_cost_per_min     int4;

alter table tariff.tariff
    rename column price_per_mile to ride_cost_per_km;




