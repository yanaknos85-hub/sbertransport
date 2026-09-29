alter table srm.srm_request_kpi rename column required_passengers to passengers;
alter table srm.srm_shared_ride drop column if exists point_matching_type;
