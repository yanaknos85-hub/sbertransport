alter table srm.srm_waypoint drop column if exists distance_from_prev_waypoint;

alter table srm.srm_shared_ride drop column if exists bunch_number;
