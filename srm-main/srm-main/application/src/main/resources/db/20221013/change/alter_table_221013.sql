alter table srm.srm_waypoint add column if not exists distance_from_prev_waypoint float8;
comment on column srm.srm_waypoint.distance_from_prev_waypoint is 'Расстояние от предыдущей точки';

alter table srm.srm_shared_ride add column if not exists bunch_number int;
comment on column srm.srm_shared_ride.bunch_number is 'Номер последнего изменения';