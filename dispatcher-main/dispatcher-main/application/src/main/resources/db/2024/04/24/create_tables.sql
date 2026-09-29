-- Create table cargo_package

create table dispatcher.cargo_package
(
    id uuid not null
        constraint dispatcher_cargo_package_pkey
            primary key,
    label varchar(128) not null,
    cost double precision not null,
    unit varchar(128) not null,
    active boolean default true not null,
    contractor uuid
        constraint dispatcher_cargo_package_contractor_fk
            references dispatcher.contractor
);

comment on table dispatcher.cargo_package is 'Справочник упаковочных материалов';

comment on column dispatcher.cargo_package.id is 'Идентификатор';

comment on column dispatcher.cargo_package.label is 'Наименование упаковки';

comment on column dispatcher.cargo_package.cost is 'Стоимость, руб';

comment on column dispatcher.cargo_package.unit is 'Единица измерения';

comment on column dispatcher.cargo_package.active is 'Признак активной записи';

comment on column dispatcher.cargo_package.contractor is 'Идентификатор контрагента';

create index dispatcher_cargo_package_contractor_idx
    on dispatcher.cargo_package (contractor);

-- Create table check_in

create table dispatcher.check_in
(
    id uuid not null
        constraint dispatcher_check_in_pkey
            primary key,
    trip_id uuid,
    longitude double precision,
    latitude double precision,
    time timestamp,
    status text,
    type text,
    time_zone text
);

comment on table dispatcher.check_in is 'Чек-ины по поездкам';

comment on column dispatcher.check_in.id is 'Идентификатор';

comment on column dispatcher.check_in.trip_id is 'Статус';

comment on column dispatcher.check_in.longitude is 'Долгота';

comment on column dispatcher.check_in.latitude is 'Широта';

comment on column dispatcher.check_in.time is 'Время';

comment on column dispatcher.check_in.status is 'Статус поездки';

comment on column dispatcher.check_in.type is 'Тип чек-ина';

comment on column dispatcher.check_in.time_zone is 'Временная зона';

create index dispatcher_checkin_tripid_idx
    on dispatcher.check_in (trip_id);

-- Create table company_sq

create table dispatcher.company_sq
(
    id uuid not null
        constraint dispatcher_company_sq_pkey
            primary key,
    prefix varchar(2) not null,
    orgdigitid numeric not null,
    sq numeric not null,
    dt_insert timestamp with time zone default now() not null,
    dt_modify timestamp with time zone default now() not null
);

comment on table dispatcher.company_sq is 'Таблица для формирования последовательностей для компаний';

comment on column dispatcher.company_sq.id is 'Уникальный идентификатор (первичный ключ)';

comment on column dispatcher.company_sq.prefix is 'Кодовое обозначение типа сущности';

comment on column dispatcher.company_sq.orgdigitid is 'ID организации (числовой)';

comment on column dispatcher.company_sq.sq is 'Порядковый номер (в рамках клиента)';

comment on column dispatcher.company_sq.dt_insert is 'Дата время вставки записи';

comment on column dispatcher.company_sq.dt_modify is 'Дата время модификации записи';

create unique index dispatcher_company_sq_prefix_orgdigitid_sq_uidx
    on dispatcher.company_sq (prefix, orgdigitid, sq);

create unique index dispatcher_company_sq_prefix_orgdigitid_uidx
    on dispatcher.company_sq (prefix, orgdigitid);

-- Create table contractor_organization

create table dispatcher.contractor_organization
(
    contractor_id uuid not null
        constraint dispatcher_contractor_organization_contractor_fk
            references dispatcher.contractor,
    organization_id uuid not null,
    constraint dispatcher_contractor_organization_pk
        primary key (contractor_id, organization_id)
);

comment on table dispatcher.contractor_organization is 'Связи контрагентов и организаций';

comment on column dispatcher.contractor_organization.contractor_id is 'Идентификатор контрагента';

comment on column dispatcher.contractor_organization.organization_id is 'Идентификатор привязанной организации';

-- Create table contractor_region

create table dispatcher.contractor_region
(
    contractor_id uuid not null
        constraint dispatcher_contractor_region_fkey
            references dispatcher.contractor,
    region_id uuid not null
);

create index dispatcher_contractor_region_contractor_idx
    on dispatcher.contractor_region (contractor_id);

create index dispatcher_contractor_region_region_idx
    on dispatcher.contractor_region (region_id);

-- Create table employee

create table dispatcher.employee
(
    id uuid not null
        constraint dispatcher_employee_pkey
            primary key,
    first_name varchar(255) default ''::character varying,
    last_name varchar(255) default ''::character varying,
    patronymic varchar(255),
    user_id uuid,
    organization_id uuid,
    active boolean default true not null,
    mobile_phone varchar(20),
    consent boolean default false not null
);

comment on table dispatcher.employee is 'Сотрудники';

comment on column dispatcher.employee.id is 'Идентификатор';

comment on column dispatcher.employee.first_name is 'Имя';

comment on column dispatcher.employee.last_name is 'Фамилия';

comment on column dispatcher.employee.patronymic is 'Отчество';

comment on column dispatcher.employee.user_id is 'Идентификатор пользователя';

comment on column dispatcher.employee.organization_id is 'Идентификатор оргранизацции';

comment on column dispatcher.employee.active is 'Флаг активности(неудаленности)';

comment on column dispatcher.employee.mobile_phone is 'Номер телефона';

comment on column dispatcher.employee.consent is 'Флаг подписания ПДн';

-- Create table imported_data

create table dispatcher.imported_data
(
    id uuid not null
        constraint dispatcher_imported_data_pkey
            primary key,
    file_name varchar(255) not null,
    import_date date not null,
    data_type varchar(255) not null,
    data jsonb,
    status varchar(50)
);

comment on table dispatcher.imported_data is 'Сессии импорта';

comment on column dispatcher.imported_data.id is 'Идентификатор сессии импорта';

comment on column dispatcher.imported_data.file_name is 'Имя файла откуда был произведен импорт';

comment on column dispatcher.imported_data.import_date is 'Дата импорта';

comment on column dispatcher.imported_data.data_type is 'Тип импортируемой сущности';

comment on column dispatcher.imported_data.data is 'Данные импорта';

comment on column dispatcher.imported_data.status is 'Статус импорта, READY_FOR_IMPORT или IMPORTED';

-- Create table messages_geo_zone

create table dispatcher.messages_geo_zone
(
    id uuid not null
        constraint dispatcher_geo_zone_pkey
            primary key,
    name text not null,
    parent_id uuid,
    code text not null
);

comment on column dispatcher.messages_geo_zone.id is 'Идентификатор';

comment on column dispatcher.messages_geo_zone.name is 'Название геозоны';

comment on column dispatcher.messages_geo_zone.parent_id is 'Идентификатор родительской геозоны';

-- Create table trips

create table dispatcher.trips
(
    id uuid not null
        constraint dispatcher_trips_pkey
            primary key,
    status text,
    requests json,
    waypoints json,
    dispatcher_id uuid,
    contractor_id uuid,
    driver_id uuid,
    vehicle_id uuid,
    start_time timestamp,
    end_time timestamp,
    arrived_date timestamp,
    fact_distance double precision,
    digit_id bigint not null,
    type varchar(9) default 'PASSENGER'::character varying not null,
    additional json,
    autoassign_counter integer default 0,
    constraint dispatcher_trips_digit_id_contractor_id_type_uk
        unique (digit_id, contractor_id, type)
);

comment on table dispatcher.trips is 'Поездки';

comment on column dispatcher.trips.id is 'Идентификатор';

comment on column dispatcher.trips.status is 'Статус';

comment on column dispatcher.trips.requests is 'Заявки';

comment on column dispatcher.trips.waypoints is 'Путевые точки';

comment on column dispatcher.trips.dispatcher_id is 'Идентификатор диспетчера';

comment on column dispatcher.trips.contractor_id is 'Идентификатор контрагента';

comment on column dispatcher.trips.driver_id is 'Идентификатор водителя';

comment on column dispatcher.trips.vehicle_id is 'Идентификатор ТС';

comment on column dispatcher.trips.start_time is 'Начальное время поездки';

comment on column dispatcher.trips.end_time is 'Предположительное конечное время поездки';

comment on column dispatcher.trips.arrived_date is 'Время прибытия водителя';

comment on column dispatcher.trips.fact_distance is 'Фактическая дистанция поездки';

comment on column dispatcher.trips.digit_id is 'Порядковый номер поездки';

comment on column dispatcher.trips.type is 'Тип поездки PASSENGER|CARGO';

comment on column dispatcher.trips.additional is 'Дополнительные данные поездки';

comment on column dispatcher.trips.autoassign_counter is 'Номер итерации автоназначения водителя';

-- Create table trip_history

create table dispatcher.trip_history
(
    change_time timestamp not null,
    trip_id uuid not null
        constraint dispatcher_trip_history_trip_fk
            references dispatcher.trips,
    old_dispatcher_id uuid
        constraint dispatcher_trip_history_old_dispatcher_fk
            references dispatcher.dispatcher,
    new_dispatcher_id uuid
        constraint dispatcher_trip_history_new_dispatcher_fk
            references dispatcher.dispatcher,
    constraint dispatcher_trip_history_pk
        primary key (change_time, trip_id)
);

comment on table dispatcher.trip_history is 'История изменения поездки';

comment on column dispatcher.trip_history.change_time is 'Время изменения данных';

comment on column dispatcher.trip_history.trip_id is 'Измененная поездка';

comment on column dispatcher.trip_history.old_dispatcher_id is 'Предыдущий диспетчер';

comment on column dispatcher.trip_history.new_dispatcher_id is 'Новый диспетчер';















