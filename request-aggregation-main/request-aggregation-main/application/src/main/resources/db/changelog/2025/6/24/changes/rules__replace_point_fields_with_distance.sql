-- Удаление устаревших полей
alter table request_aggregation.rules
drop column point_start,
drop column point_end;

-- Добавление новых полей
alter table request_aggregation.rules
    add column max_time_travel integer not null,
add column deviation_time integer not null,
add column min_dst_km integer not null,
add column max_dst_km integer not null;