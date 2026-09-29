create schema if not exists request_aggregation;
comment
on schema request_aggregation is 'Сбор данных';

create table request_aggregation.history_log
(
    id     uuid         not null
        constraint history_log_pkey
            primary key,
    create_date_time  timestamp    not null,
    doc_format        varchar(50) not null,
    count_records     numeric      not null,
    sender            varchar(255) not null,
    processing_status  numeric      not null,
    description_status varchar(255)
);

comment
on column request_aggregation.history_log.id is 'Идентификатор лога';

comment
on column request_aggregation.history_log.create_date_time is 'Дата создания';

comment
on column request_aggregation.history_log.doc_format is 'Формат';

comment
on column request_aggregation.history_log.count_records is 'Количество записей';

comment
on column request_aggregation.history_log.sender is 'Отправитель';

comment
on column request_aggregation.history_log.processing_status is 'Статус обработки';

comment
on column request_aggregation.history_log.description_status is 'Описание статуса';



create table request_aggregation.transport_type
(
    id                  uuid         not null
        constraint transport_type_pkey
            primary key,
    transport_type_name varchar(255) not null
);

comment
on column request_aggregation.transport_type.id is 'Идентификатор контакта';

comment
on column request_aggregation.transport_type.transport_type_name is 'Название типа транспорта';


create table request_aggregation.trip_type
(
    id        uuid         not null
        constraint trip_type_pkey
            primary key,
    trip_name varchar(255) not null
);

comment
on column request_aggregation.trip_type.id is 'Идентификатор типа поездки';

comment
on column request_aggregation.trip_type.trip_name is 'Название типа поездки';


create table request_aggregation.status_history
(
    id              uuid         not null
        constraint status_history_pkey
            primary key,
    change_datetime timestamp    not null,
    previous_status varchar(255) not null,
    next_status     varchar(255) not null
);

comment
on column request_aggregation.status_history.id is 'Идентификатор статуса';

comment
on column request_aggregation.status_history.change_datetime is 'Дата изменения статуса';

comment
on column request_aggregation.status_history.previous_status is 'Предыдущий статус';

comment
on column request_aggregation.status_history.next_status is 'Следующий статус';


create table request_aggregation.status
(
    id              uuid         not null
        constraint status_pkey
            primary key,
    status_name varchar(255) not null
);

comment
on column request_aggregation.status.id is 'Идентификатор статуса';

comment
on column request_aggregation.status.status_name is 'Название статуса';


create table request_aggregation.lead
(
    id                uuid      not null
        constraint lead_pkey
            primary key,
    status_id         uuid      not null,
    create_datetime   timestamp not null,
    trip_id           uuid      not null,
    transport_type_id uuid      not null,
    passenger_count   numeric   not null,
    comment           text,
    departure_time    timestamp not null
);

comment
on column request_aggregation.lead.id is 'Идентификатор лида';

comment
on column request_aggregation.lead.status_id is 'Статус';

comment
on column request_aggregation.lead.create_datetime is 'Дата создания';

comment
on column request_aggregation.lead.trip_id is 'Идентификатор поездки';

comment
on column request_aggregation.lead.transport_type_id is 'Тип транспорта';

comment
on column request_aggregation.lead.passenger_count is 'Количество пассажиров';

comment
on column request_aggregation.lead.comment is 'Комментарий';

comment
on column request_aggregation.lead.departure_time is 'Время отправления';


create table request_aggregation.users
(
    id              uuid      not null
        constraint user_pkey
            primary key,
    create_datetime timestamp not null,
    contact_info_id uuid      not null
);

comment
on column request_aggregation.users.id is 'Идентификатор пользователя';

comment
on column request_aggregation.users.create_datetime is 'Дата создания';

comment
on column request_aggregation.users.contact_info_id is 'Контактная информация';


create table request_aggregation.contact_information
(
    id              uuid         not null
        constraint contact_information_pkey
            primary key,
    first_name      varchar(100) not null,
    last_name       varchar(100) not null,
    patronymic      varchar(100)
);

comment
on column request_aggregation.contact_information.id is 'Идентификатор контакта';

comment
on column request_aggregation.contact_information.first_name is 'Имя';

comment
on column request_aggregation.contact_information.last_name is 'Фамилия';

comment
on column request_aggregation.contact_information.patronymic is 'Отчество';


create table request_aggregation.lead_user
(
    id        uuid    not null
        constraint lead_user_pkey
            primary key,
    lead_id   uuid    not null,
    user_id   uuid    not null,
    is_driver boolean not null
);

comment
on column request_aggregation.lead_user.id is 'Идентификатор связи лида и пользователя';

comment
on column request_aggregation.lead_user.lead_id is 'Идентификатор лида';

comment
on column request_aggregation.lead_user.user_id is 'Идентификатор пользователя';

comment
on column request_aggregation.lead_user.is_driver is 'Является водителем';


create table request_aggregation.contact_link
(
    id               uuid not null
        constraint contact_link_pkey
            primary key,
    type_link        varchar(100) not null,
    value_link       varchar(255) not null,
    create_date_link timestamp not null,
    delete_date_link timestamp,
    contact_information_id uuid not null
);

comment
on column request_aggregation.contact_link.id is 'Идентификатор связи контакта';

comment
on column request_aggregation.contact_link.type_link is 'Тип связи';

comment
on column request_aggregation.contact_link.value_link is 'Значение';

comment
on column request_aggregation.contact_link.create_date_link is 'Дата создания связи';

comment
on column request_aggregation.contact_link.delete_date_link is 'Дата удаления связи';

comment
on column request_aggregation.contact_link.contact_information_id is 'Идентификатор контакта';


create table request_aggregation.main_lead
(
    id              uuid      not null
        constraint main_lead_pkey
            primary key,
    status_id       uuid      not null,
    create_datetime timestamp not null,
    is_application  boolean   not null
);

comment
on column request_aggregation.main_lead.id is 'Идентификатор главного лида';

comment
on column request_aggregation.main_lead.status_id is 'Статус';

comment
on column request_aggregation.main_lead.create_datetime is 'Дата создания';

comment
on column request_aggregation.main_lead.is_application is 'Является заявкой';


create table request_aggregation.point_lead
(
    id              uuid      not null
        constraint point_lead_pkey
            primary key,
    main_lead_id uuid,
    lead_id      uuid    not null,
    type_point   varchar(255) not null,
    longitude    float   not null,
    latitude     float   not null,
    waypoint     varchar(255)
);

comment
on column request_aggregation.point_lead.main_lead_id is 'Идентификатор главного лида';

comment
on column request_aggregation.point_lead.lead_id is 'Идентификатор лида';

comment
on column request_aggregation.point_lead.type_point is 'Тип точки';

comment
on column request_aggregation.point_lead.longitude is 'Долгота';

comment
on column request_aggregation.point_lead.latitude is 'Широта';

comment
on column request_aggregation.point_lead.waypoint is 'Промежуточная точка';


create table request_aggregation.user_address
(
    id         uuid not null
        constraint user_address_pkey
            primary key,
    address_id uuid not null,
    client_id  uuid not null
);

comment
on column request_aggregation.user_address.id is 'Идентификатор адреса пользователя';

comment
on column request_aggregation.user_address.address_id is 'Идентификатор адреса';

comment
on column request_aggregation.user_address.client_id is 'Идентификатор клиента';


create table request_aggregation.address
(
    id           uuid         not null
        constraint address_pkey
            primary key,
    address_name varchar(255) not null,
    street       varchar(255) not null,
    house        varchar(50)  not null,
    city         varchar(255) not null,
    region       varchar(255) not null,
    country      varchar(255) not null,
    region_type  varchar(255) not null
);

comment
on column request_aggregation.address.id is 'Идентификатор адреса';

comment
on column request_aggregation.address.address_name is 'Название';

comment
on column request_aggregation.address.street is 'Улица';

comment
on column request_aggregation.address.house is 'Дом';

comment
on column request_aggregation.address.city is 'Город';

comment
on column request_aggregation.address.region is 'Регион';

comment
on column request_aggregation.address.country is 'Страна';

comment
on column request_aggregation.address.region_type is 'Тип региона';


alter table request_aggregation.contact_link
    add constraint contact_information_id_fkey
        foreign key (contact_information_id) references request_aggregation.contact_information;


alter table request_aggregation.lead
    add constraint lead_status_id_fkey
        foreign key (status_id) references request_aggregation.status;

alter table request_aggregation.lead
    add constraint trip_id_fkey
        foreign key (trip_id) references request_aggregation.trip_type;

alter table request_aggregation.lead
    add constraint transport_type_id_fkey
        foreign key (transport_type_id) references request_aggregation.transport_type;


alter table request_aggregation.users
    add constraint contact_info_id_fkey
        foreign key (contact_info_id) references request_aggregation.contact_information;


alter table request_aggregation.lead_user
    add constraint lead_id_lead_user_fkey
        foreign key (lead_id) references request_aggregation.lead;

alter table request_aggregation.lead_user
    add constraint user_id_fkey
        foreign key (user_id) references request_aggregation.users;


alter table request_aggregation.main_lead
    add constraint status_id_fkey
        foreign key (status_id) references request_aggregation.status;


alter table request_aggregation.point_lead
    add constraint main_lead_id_fkey
        foreign key (main_lead_id) references request_aggregation.main_lead;

alter table request_aggregation.point_lead
    add constraint lead_id_point_lead_fkey
        foreign key (lead_id) references request_aggregation.lead;


alter table request_aggregation.user_address
    add constraint address_id_fkey
        foreign key (address_id) references request_aggregation.address;

alter table request_aggregation.user_address
    add constraint client_id_fkey
        foreign key (client_id) references request_aggregation.users;



create table request_aggregation.rules
(
    id     uuid         not null
        constraint rules_pkey
            primary key,
    time_start  timestamp    not null,
    geozone     numeric      not null,
    point_start varchar(255) not null,
    point_end   varchar(255) not null,
    deviation   numeric      not null
);

comment
on column request_aggregation.rules.time_start is 'Время начала действия правила';

comment
on column request_aggregation.rules.geozone is 'Идентификатор геозоны';

comment
on column request_aggregation.rules.point_start is 'Начальная точка маршрута';

comment
on column request_aggregation.rules.point_end is 'Конечная точка маршрута';

comment
on column request_aggregation.rules.deviation is 'Допустимое отклонение';
