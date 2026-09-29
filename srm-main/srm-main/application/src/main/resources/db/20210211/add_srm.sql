create table srm.srm_shared_ride
(
    id                uuid    primary key,
    old_id            serial8,
    passengers        int4    check (passengers >= 1),
    active            boolean not null default true,
    tariff_id         uuid,
    transport_type    varchar(255) not null,
    ride_cost         int8,
    ride_distance     float8,
    ride_time         int4,
    savings_deviation_pct float8,
    time_deviation_min    int4,
    distance_deviation_km float8,
    min_cancel_time_min   int4
);

comment on table srm.srm_shared_ride is 'Совместная поездка';
comment on column srm.srm_shared_ride.id is 'Id';
comment on column srm.srm_shared_ride.passengers is 'Количество пассажиров';
comment on column srm.srm_shared_ride.active is 'Флаг активности';
comment on column srm.srm_shared_ride.tariff_id is 'Id используемого тарифа';
comment on column srm.srm_shared_ride.transport_type is 'Тип транспорта';
comment on column srm.srm_shared_ride.ride_cost is 'kpi';
comment on column srm.srm_shared_ride.ride_distance is 'Расчитанное общее расстояние в км';
comment on column srm.srm_shared_ride.ride_time is 'Расчитанное общее время в секундах';
comment on column srm.srm_shared_ride.savings_deviation_pct is 'Минимальный процент экономии';
comment on column srm.srm_shared_ride.time_deviation_min is 'Максимальное время отклонения';
comment on column srm.srm_shared_ride.distance_deviation_km is 'Максимальный километраж отклонения';
comment on column srm.srm_shared_ride.min_cancel_time_min is 'Триггерное время';

create table if not exists srm.srm_waypoint
(
    id             uuid        primary key,
    shared_ride_id uuid        not null references srm.srm_shared_ride,
    request_kpi_id uuid        not null,
    group_id       uuid,
    latitude       double precision not null,
    longitude      double precision not null,
    active         boolean     not null,
    event_type     varchar(50) not null,
    start_time     timestamp WITH TIME ZONE,
    end_time       timestamp WITH TIME ZONE,
    waiting_time   int4,
    address        varchar(255),
    org_ordering_index int4,
    ordering_index int4
);

comment on table srm.srm_waypoint is 'Точка маршрута совместной поездки';
comment on column srm.srm_waypoint.id is 'Id';
comment on column srm.srm_waypoint.shared_ride_id is 'Id заказа Magenta';
comment on column srm.srm_waypoint.request_kpi_id is 'Id связанной заявки';
comment on column srm.srm_waypoint.latitude is 'Широта';
comment on column srm.srm_waypoint.longitude is 'Долгота';
comment on column srm.srm_waypoint.active is 'Флаг активности';
comment on column srm.srm_waypoint.event_type is 'Тип события';
comment on column srm.srm_waypoint.start_time  is 'Время начала остановки';
comment on column srm.srm_waypoint.end_time is 'Время завершения остановки';
comment on column srm.srm_waypoint.waiting_time is 'Время ожидания';
comment on column srm.srm_waypoint.address is 'Адрес';
comment on column srm.srm_waypoint.org_ordering_index is 'Индекс в изначальной заявке';
comment on column srm.srm_waypoint.ordering_index is 'Индекс в совместной поездке';

create table srm.srm_request_kpi
(
    id                uuid primary key,
    old_id            serial8,
    pickup_time       timestamp WITH TIME ZONE,
    drop_time         timestamp WITH TIME ZONE,
    passengers        int4    check (passengers >= 1),
    creation_time     timestamp,
    ordering_index    serial8,
    cost_share_part   float8,
    request_distance  float8,
    request_time      int4,
    request_price     int8,
    savings_cash      int8 check (savings_cash >= 0),
    savings_procents  float4 check (savings_procents >= 0),
    shared_ride_id uuid references srm.srm_shared_ride
);

comment on table srm.srm_request_kpi is 'KPI отдельной заявки в составе совместной поездки';
comment on column srm.srm_request_kpi.id is 'Id заявки';
comment on column srm.srm_request_kpi.pickup_time  is 'Время начала';
comment on column srm.srm_request_kpi.drop_time is 'Время завершения';
comment on column srm.srm_request_kpi.passengers is 'Количество пассажиров';
comment on column srm.srm_request_kpi.creation_time is 'Время добавления в совместную поездку';
comment on column srm.srm_request_kpi.ordering_index is 'Порядковый номер';
comment on column srm.srm_request_kpi.cost_share_part is 'Доля стоимости заказа от общей';
comment on column srm.srm_request_kpi.request_distance is 'Расстояние отрезка пути в километрах';
comment on column srm.srm_request_kpi.request_time is 'Время отрезка пути в секундах';
comment on column srm.srm_request_kpi.request_price is 'Цена отрезка пути по тарифу';
comment on column srm.srm_request_kpi.savings_cash is 'Экономия в копейках';
comment on column srm.srm_request_kpi.savings_procents is 'Процент экономии';
comment on column srm.srm_request_kpi.shared_ride_id is 'Id родительского объекта';



