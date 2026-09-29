alter table request.request_for_taxi
add column if not exists vehicle_id uuid;

comment on column request.request_for_taxi.vehicle_id is 'Транспорт';