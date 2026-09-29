alter table notifications_request.taxi_trip drop column magenta_id;
alter table notifications_request.taxi_trip add column shared_ride_id UUID;

delete from notifications_request.shared_trip;
alter table notifications_request.shared_trip drop column magenta_id;
alter table notifications_request.shared_trip add column id UUID not null constraint position_pkey primary key;

alter table notifications_request.trip drop column shared_ride_id;
alter table notifications_request.trip add column shared_ride_id UUID;
