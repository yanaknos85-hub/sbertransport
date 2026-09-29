alter table request_aggregation.point_lead

    add column point_number              numeric;

comment on column request_aggregation.point_lead.point_number is 'Порядковый номер точки в основном лиде';

alter table request_aggregation.lead

    drop column passenger_count;

alter table request_aggregation.transport_type
    add column max_passenger            numeric;

alter table request_aggregation.transport_type
    add column sub_type_name            text;

comment
on column request_aggregation.transport_type.max_passenger is 'Максимальное количество пассажиров';

comment
on column request_aggregation.transport_type.sub_type_name is 'Название вида транспорта';