alter table srm.tariff drop column if exists max_weight;

alter table srm.tariff drop column if exists max_volume;

alter table srm.tariff drop column if exists max_route_length;

alter table srm.tariff drop column if exists max_waypoint_count;

alter table srm.tariff drop column if exists tariff_km;

alter table srm.tariff drop column if exists cost_loader;

alter table srm.tariff drop column if exists express;

alter table srm.srm_request_kpi drop column if exists required_volume;

alter table srm.srm_request_kpi drop column if exists required_weight;

alter table srm.srm_request_kpi drop column if exists active;

alter table srm.srm_request_kpi drop column if exists cargo_express;

alter table srm.srm_shared_ride drop column if exists bunch_id;

alter table srm.srm_waypoint drop column if exists loader_number;
