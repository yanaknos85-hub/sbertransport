alter table srm.srm_shared_ride add creation_time timestamp;
comment on column srm.srm_shared_ride.creation_time is 'Триггерное время';

alter table srm.srm_waypoint alter column start_time set data type timestamp without time zone;
alter table srm.srm_waypoint alter column end_time set data type timestamp without time zone;
alter table srm.srm_request_kpi alter column pickup_time set data type timestamp without time zone;
alter table srm.srm_request_kpi alter column drop_time set data type timestamp without time zone;
