alter table tariff.tariff add column if not exists trigger_time int4 default 60;

comment on column tariff.tariff.trigger_time is 'Триггерное время';