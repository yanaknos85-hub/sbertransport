alter table request.request_for_personal drop column ride_id;
alter table request.request_for_personal drop column shared_ride_owner;

alter table request.request_for_taxi drop column ride_id;
alter table request.request_for_taxi drop column shared_ride_owner;

alter table request.request_for_carsharing drop column ride_id;
alter table request.request_for_carsharing drop column shared_ride_owner;

alter table request.taxi_trip drop column ride_id;