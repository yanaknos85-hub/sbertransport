ALTER TABLE reports.taxi_trip
    ADD COLUMN intermediate_wait_time bigint;

comment on column reports.taxi_trip.intermediate_wait_time is 'Время ожидания в промежуточных точках, мин';