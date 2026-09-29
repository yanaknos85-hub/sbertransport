ALTER TABLE reports.taxi_trip_registry_string
    ADD COLUMN intermediate_wait_time int;

comment on column reports.taxi_trip_registry_string.intermediate_wait_time is 'Время ожидания в промежуточных точках, мин';