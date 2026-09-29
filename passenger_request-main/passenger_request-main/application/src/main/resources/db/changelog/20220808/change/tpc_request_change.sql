alter table request.request_for_personal add column ride_id uuid;
comment on column request.request_for_personal.ride_id is 'ID совместной поездки';
alter table request.request_for_personal add column shared_ride_owner bool default false;
comment on column request.request_for_personal.shared_ride_owner is 'Флаг владельца совместной поездки';

alter table request.request_for_taxi add column ride_id uuid;
comment on column request.request_for_taxi.ride_id is 'ID совместной поездки';
alter table request.request_for_taxi add column shared_ride_owner bool default false;
comment on column request.request_for_taxi.shared_ride_owner is 'Флаг владельца совместной поездки';

alter table request.request_for_carsharing add column ride_id uuid;
comment on column request.request_for_carsharing.ride_id is 'ID совместной поездки';
alter table request.request_for_carsharing add column shared_ride_owner bool default false;
comment on column request.request_for_carsharing.shared_ride_owner is 'Флаг владельца совместной поездки';

alter table request.taxi_trip add column ride_id uuid;
comment on column request.taxi_trip.ride_id is 'ID совместной поездки';