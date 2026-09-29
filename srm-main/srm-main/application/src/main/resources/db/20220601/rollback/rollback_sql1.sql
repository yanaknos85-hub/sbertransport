alter table srm.srm_shared_ride drop column if exists creation_time;

alter table srm.srm_waypoint alter column start_time set data type timestamp with time zone;
alter table srm.srm_waypoint alter column end_time set data type timestamp with time zone;
alter table srm.srm_request_kpi alter column pickup_time set data type timestamp with time zone;
alter table srm.srm_request_kpi alter column drop_time set data type timestamp with time zone;
