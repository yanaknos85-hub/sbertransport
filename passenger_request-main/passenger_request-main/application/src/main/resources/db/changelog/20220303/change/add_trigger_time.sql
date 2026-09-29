alter table request.taxi_tariff add column if not exists trigger_time int4 default 60;

comment on column request.taxi_tariff.trigger_time is 'Триггерное время';

alter table request.request_for_taxi add column if not exists trigger_time int4 default 60;

comment on column request.request_for_taxi.trigger_time is 'Триггерное время';