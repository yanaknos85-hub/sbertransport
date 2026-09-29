create table if not exists srm.tariff
(
    id uuid not null,
    organization_id uuid,
    transport_type varchar (50),
    humanreadableid varchar(100),
    service_type varchar(255),
    region varchar(255),
    active boolean default true,
    contract_id uuid,
    car_service_cost int4,
    ride_cost_per_km int4,
    ride_cost_per_min int4,
    taxi_class varchar (255),
    wait_cost_per_min int4,
    wait_cost_per_min_intermediate int4 default 0,
    min_ride_distance_cost int4 default 0,
    min_ride_time_cost int4 default 0,
    max_capacity int4 default 0,

    free_waiting_time int4  default 0,
    distance_included float8 default 0,
    minutes_included  int4 default 0,

    savings_deviation_pct float8,
    time_deviation_min    int4,
    distance_deviation_km float8,
    min_cancel_time_min   int4,

    coef_work_day_morning          float8 default 1,
    coef_work_day_noon             float8 default 1,
    coef_work_day_evening          float8 default 1,
    coef_work_day_night            float8 default 1,
    coef_day_off                   float8 default 1,

    coef_engine_1_6                float8 default 1,
    coef_engine_1_6_to_2_0         float8 default 1,
    coef_engine_2_0_to_2_5         float8 default 1,
    seasonal_coefficient           float8,
    season_start                   date,
    season_end                     date,
    coef_traffic                   float8 default 0,
    coef_material_assets           float8 default 1,

    suburb_service_cost_per_km     int4   default 0,
    suburb_service_cost_per_min    int4   default 0,
    cost_per_min_inter_region      int4   default 0,
    cost_per_km_inter_region       int4   default 0,
    cost_per_min_suburb            int4   default 0,
    cost_per_km_suburb             int4   default 0,

    contractor_max_diff_computed_distance_percent int2
        check(contractor_max_diff_computed_distance_percent >= 0 AND contractor_max_diff_computed_distance_percent <= 100),
    contractor_max_diff_fact_distance_percent int2
        check(contractor_max_diff_fact_distance_percent >= 0 AND contractor_max_diff_fact_distance_percent <= 100),
    contractor_max_diff_computed_cost_percent int2
        check(contractor_max_diff_computed_cost_percent >= 0 AND contractor_max_diff_computed_cost_percent <= 100),
    contractor_max_diff_contractor_cost_percent int2
        check(contractor_max_diff_contractor_cost_percent >= 0 AND contractor_max_diff_contractor_cost_percent <= 100),
    contractor_max_diff_computed_waiting_percent int2
        check(contractor_max_diff_computed_waiting_percent >= 0 AND contractor_max_diff_computed_waiting_percent <= 100),
    PRIMARY KEY (id)
);

comment on table srm.tariff is 'Тариф';
comment on column srm.tariff.id is 'Id';
comment on column srm.tariff.savings_deviation_pct is 'Минимальный процент экономии';
comment on column srm.tariff.time_deviation_min is 'Максимальное время отклонения';
comment on column srm.tariff.distance_deviation_km is 'Максимальный километраж отклонения';
comment on column srm.tariff.min_cancel_time_min is 'Триггерное время';


