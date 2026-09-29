create table request.group_transfer_trip
(
    trip_type                    varchar(50)          not null,
    id                           uuid                 not null
        constraint group_transfer_trip_pkey
            primary key,
    date_time_registered         timestamp,
    organization_id              uuid                 not null
        constraint group_transfer_trip_organization_id_fkey
            references request.organization,
    tariff_id                    uuid                 not null,
    trip_finish_time             timestamp,
    trip_start_time              timestamp,
    request_id                   uuid,
    status                       varchar(255),
    trip_fact_distance           double precision,
    trip_fact_duration           bigint,
    trip_fact_price              integer,
    trip_fact_wait_time          bigint,
    trip_assignment_date_time    timestamp,
    active                       boolean default true not null,
    human_readable_id            text
        constraint request_group_transfer_trip_human_readable_id_uk
            unique,
    contractor_comment           varchar(255),
    resolution                   varchar(2000),
    decision_code                varchar(255),
    car_brand_name               varchar(128),
    car_model                    varchar(128),
    car_color                    varchar(128),
    car_registration_number      varchar,
    time_work_start              timestamp,
    time_work_finish             timestamp,
    last_known_position_id       uuid
        constraint group_transfer_trip_last_known_position_id_fkey
            references request.route_history_element,
    last_xml_received_date_time  timestamp,
    fact_parameters_setting_time timestamp,
    driver                       text
);

comment on table request.group_transfer_trip is 'Поездка на такси';

comment on column request.group_transfer_trip.trip_type is 'Дата и время начала поездки';

comment on column request.group_transfer_trip.id is 'Идентификатор';

comment on column request.group_transfer_trip.date_time_registered is 'Дата и время регистрации в системе исполнителя';

comment on column request.group_transfer_trip.organization_id is 'Идентификатор организации, к которой принадлежат пассажиры';

comment on column request.group_transfer_trip.trip_finish_time is 'Дата и время завершения поездки';

comment on column request.group_transfer_trip.request_id is 'Связанная индивидуальная поездка';

comment on column request.group_transfer_trip.status is 'Статус из системы исполнителя';

comment on column request.group_transfer_trip.trip_fact_distance is 'Километраж';

comment on column request.group_transfer_trip.trip_fact_duration is 'Длительность поездки';

comment on column request.group_transfer_trip.trip_fact_price is 'Стоимость заявки';

comment on column request.group_transfer_trip.trip_fact_wait_time is 'Время простоя ТС';

comment on column request.group_transfer_trip.trip_assignment_date_time is 'Дата и время назначения водителя на заявку';

comment on column request.group_transfer_trip.active is 'Флаг активности(false - удалено)';

comment on column request.group_transfer_trip.human_readable_id is 'Человекочитаемый идентификатор';

comment on column request.group_transfer_trip.contractor_comment is 'Комментарий исполнителя';

comment on column request.group_transfer_trip.resolution is 'Решение (изменяется контрагентом)';

comment on column request.group_transfer_trip.decision_code is 'Код закрытия (изменяется контрагентом)';

comment on column request.group_transfer_trip.car_brand_name is 'Брэнд автомобиля';

comment on column request.group_transfer_trip.car_model is 'Модель автомобиля';

comment on column request.group_transfer_trip.car_color is 'Цвет автомобиля';

comment on column request.group_transfer_trip.car_registration_number is 'Номер автомобиля';

comment on column request.group_transfer_trip.time_work_start is 'Время начала работ';

comment on column request.group_transfer_trip.time_work_finish is 'Время  завершения работ';

comment on column request.group_transfer_trip.driver is 'Информация о водителе';


create table request.request_for_group_transfer
(
    id                                        uuid                         not null
        constraint request_for_group_transfer_pkey
            primary key,
    humanreadableid                           varchar(100)                 not null
        constraint request_for_group_transfer_humanreadableid_key
            unique,
    author_id                                 uuid                         not null
        constraint fk_request_for_group_transfer_author
            references request.employee,
    passenger_id                              uuid                         not null
        constraint fk_request_for_group_transfer_passenger
            references request.employee,
    creation_time                             timestamp,
    transport_type                            varchar(255)                 not null,
    approved_by_id                            uuid
        constraint fk_request_for_group_transfer_approved
            references request.employee,
    approval_date                             timestamp,
    segments                                  jsonb,
    tariff_id                                 uuid,
    request_status                            varchar(255),
    status_code                               integer,
    approval_state                            varchar(255),
    expected_cost                             double precision,
    expected_distance                         double precision,
    expected_time                             bigint,
    trip_purpose                              uuid
        constraint fk_request_for_group_transfer_purpose
            references request.trip_purpose,
    request_options                           jsonb,
    desired_date                              timestamp                    not null,
    passenger_count                           integer                      not null,
    trip_class                                varchar(255),
    comment                                   varchar(255),
    rating_advantages                         jsonb,
    rating_drawbacks                          jsonb,
    rating_mark                               integer,
    rating_comment                            varchar(255),
    finished_time                             timestamp,
    contractor_id                             uuid,
    driver_id                                 uuid,
    dispatcher_id                             uuid,
    auto_cancel_deadline_min                  integer default 15,
    sent_to_contractor                        boolean default false        not null,
    deadline_state                            text                         not null,
    active                                    boolean default true         not null,
    group_transfer_trip_id                    uuid
        constraint request_for_group_transfer_group_transfer_trip_id_fkey
            references request.group_transfer_trip,
    resolution                                varchar(2000),
    time_zone                                 varchar(100),
    group_transfer_awaiting_search_start_date timestamp,
    vehicle_id                                uuid,
    driver_assignment_deadline                timestamp,
    trigger_time                              integer default 60,
    bonus_cost                                bigint,
    ride_id                                   uuid,
    shared_ride_owner                         boolean default false,
    organization_id                           uuid,
    bus_rent_duration                         integer,
    bus_count                                 integer,
    fact_waiting_time                         bigint,
    fact_distance                             double precision,
    cost_share_part                           double precision,
    savings_cash                              bigint,
    savings_procents                          bigint,
    driver_arrived_datetime                   timestamp,
    driver_arrived_deadline                   timestamp,
    employee_device_time_zone                 text,
    number_passengers_joined                  integer,
    tariff                                    jsonb,
    approval_deadline                         timestamp,
    approval_deadline_state                   text    default 'NONE'::text not null,
    request_closed_datetime                   timestamp
);

comment on column request.request_for_group_transfer.contractor_id is 'Контрагент - перевозчик';

comment on column request.request_for_group_transfer.driver_id is 'ID водителя';

comment on column request.request_for_group_transfer.dispatcher_id is 'ID ответственного диспетчера';

comment on column request.request_for_group_transfer.auto_cancel_deadline_min is 'Минимальное время до планируемого для согласования';

comment on column request.request_for_group_transfer.sent_to_contractor is 'Флаг отправки исполнителю (true - отправлено)';

comment on column request.request_for_group_transfer.deadline_state is 'Индикатор контрольного срока (ordinal): 0 - NONE, 1 - YELLOW, 2 - RED';

comment on column request.request_for_group_transfer.active is 'Флаг активности';

comment on column request.request_for_group_transfer.resolution is 'Описание состояния поездки от исполнителя';

comment on column request.request_for_group_transfer.time_zone is 'Таймзона созданной заявки';

comment on column request.request_for_group_transfer.group_transfer_awaiting_search_start_date is 'Дата и время старта поиска водителя на такси';

comment on column request.request_for_group_transfer.vehicle_id is 'Транспорт';

comment on column request.request_for_group_transfer.driver_assignment_deadline is 'Контрольный срок по назначению водителя';

comment on column request.request_for_group_transfer.trigger_time is 'Триггерное время';

comment on column request.request_for_group_transfer.ride_id is 'ID совместной поездки';

comment on column request.request_for_group_transfer.shared_ride_owner is 'Флаг владельца совместной поездки';

comment on column request.request_for_group_transfer.bus_rent_duration is 'Время аренды (автобусы) в мс';

comment on column request.request_for_group_transfer.bus_count is 'Кол-во автобусов';

comment on column request.request_for_group_transfer.fact_waiting_time is 'Время ожидания пассажира водителем';

comment on column request.request_for_group_transfer.fact_distance is 'Фактическая дистанция поездки (км)';

comment on column request.request_for_group_transfer.cost_share_part is 'Коэффициент части оплаты';

comment on column request.request_for_group_transfer.savings_cash is 'Экономия в рублях';

comment on column request.request_for_group_transfer.savings_procents is 'Экономия в процентах';

comment on column request.request_for_group_transfer.driver_arrived_datetime is 'Время перехода заявки в статус Водитель ожидает в точке отправления';

comment on column request.request_for_group_transfer.driver_arrived_deadline is 'Максимальная дата прибытия водителя, без нарушения SLA';

comment on column request.request_for_group_transfer.employee_device_time_zone is 'Временная зона устройства сотрудника';

comment on column request.request_for_group_transfer.number_passengers_joined is 'Количество присоединившихся пассажиров (заявок)';

comment on column request.request_for_group_transfer.tariff is 'Тарифф';

comment on column request.request_for_group_transfer.approval_deadline is 'Максимальная дата согласования заявки без нарушения контрольного срока';

comment on column request.request_for_group_transfer.approval_deadline_state is 'Индикатор контрольного срока согласования заявки';

comment on column request.request_for_group_transfer.request_closed_datetime is 'Дата получение всей информации по поездке или принудительное закрытие системой';

create table request.request_for_group_transfer_history
(
    id                            uuid                                               not null
        constraint request_for_group_transfer_history_pkey
            primary key,
    change_date                   timestamp                                          not null,
    code                          integer,
    comment                       varchar(255),
    request_status                varchar(255)                                       not null,
    request_for_group_transfer_id uuid                                               not null
        constraint fk_request_history_request_for_group_transfer
            references request.request_for_group_transfer,
    initiator_id                  uuid                                               not null,
    initiator_description         varchar(128) default 'EMPLOYEE'::character varying not null
);

comment on column request.request_for_group_transfer_history.initiator_description is 'Тип сотрудника';
