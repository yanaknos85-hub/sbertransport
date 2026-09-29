-- Create contractor table
create table dispatcher.contractor
(
    id uuid not null
        constraint dispatcher_contractor_pkey
            primary key,
    name varchar(255) not null,
    msrn varchar(20) not null,
    tin varchar(20) not null,
    contact_phone_number varchar(50),
    contact_person_info varchar(1000),
    rating integer,
    img varchar(2000),
    digit_id bigint generated always as identity,
    contractor_name varchar(255),
    contractor_rus_name varchar(255),
    integration_email varchar(255),
    active boolean default true not null,
    login varchar(255),
    password varchar(255),
    url varchar(255),
    integration_type text default 'EMAIL_XML_API'::text,
    main_dispatcher_id uuid,
    autoassign boolean default false not null,
    employee_count integer default 0 not null,
    constraint dispatcher_contractor_tin_name_uk
        unique (tin, name)
);

comment on column dispatcher.contractor.contact_phone_number is 'Контактный телефон';

comment on column dispatcher.contractor.contact_person_info is 'Информация о контактном лице';

comment on column dispatcher.contractor.rating is 'Рейтинг контрагента, от 0 до 500';

comment on column dispatcher.contractor.img is 'URL логотипа';

comment on column dispatcher.contractor.digit_id is 'Цифровой идентификатор';

comment on column dispatcher.contractor.contractor_name is 'Название контрагента латиницей';

comment on column dispatcher.contractor.contractor_rus_name is 'Название контрагента кириллицей';

comment on column dispatcher.contractor.integration_email is 'Интеграционный email контрагента';

comment on column dispatcher.contractor.login is 'Логин для авторизации в системе контрагента';

comment on column dispatcher.contractor.password is 'Пароль для авторизации в системе контрагента';

comment on column dispatcher.contractor.url is 'URL системы контрагента';

comment on column dispatcher.contractor.integration_type is 'Тип интеграционного взаимодействия';

comment on column dispatcher.contractor.main_dispatcher_id is 'Идентификатор основного диспетчера';

comment on column dispatcher.contractor.autoassign is 'Автоназначение заявок';

comment on column dispatcher.contractor.employee_count is 'Количество сотрудников КА';

create index dispatcher_contractor_active_idx
    on dispatcher.contractor (active);

-- Create attribute table

create table dispatcher.attribute
(
    id uuid not null
        constraint dispatcher_attribute_pkey
            primary key,
    contractor_id uuid
        constraint dispatcher_attribute_contractor_fk
            references dispatcher.contractor,
    name varchar(128) default ''::character varying not null,
    status varchar(256)
);

comment on column dispatcher.attribute.status is 'Признак активности';

create index dispatcher_attributes_contractor_idx
    on dispatcher.attribute (contractor_id);

create unique index dispatcher_attribute_name_contractor_unique_index
    on dispatcher.attribute (name, contractor_id);

comment on index dispatcher.dispatcher_attribute_name_contractor_unique_index is 'Уникальность признака водителя в рамках контрагента';

-- Create table autopark

create table dispatcher.autopark
(
    id uuid not null
        constraint dispatcher_autopark_pkey
            primary key,
    name varchar not null,
    contractor_id uuid
        constraint dispatcher_contractor_fk
            references dispatcher.contractor,
    active boolean default true not null
);

comment on column dispatcher.autopark.id is 'ID автопарка';

comment on column dispatcher.autopark.name is 'Название автопарка';

comment on column dispatcher.autopark.contractor_id is 'id связанного контрактора';

comment on column dispatcher.autopark.active is 'Активен';

create index dispatcher_autopark_contractor_idx
    on dispatcher.autopark (contractor_id);

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

-- Create table dispatcher

create table dispatcher.dispatcher
(
    id uuid not null
        constraint dispatcher_dispatcher_pk
            primary key,
    human_readable_id text not null
        constraint dispatcher_dispatcher_human_readable_id_uk
            unique,
    last_name text not null,
    first_name text not null,
    patronymic text,
    phone text not null,
    email text not null,
    contractor_id uuid
        constraint dispatcher_dispatcher_contractor_fk
            references dispatcher.contractor,
    active boolean default true not null,
    consent boolean default false not null
);

comment on table dispatcher.dispatcher is 'Диспетчеры';

comment on column dispatcher.dispatcher.id is 'Идентификатор';

comment on column dispatcher.dispatcher.human_readable_id is 'Человекочитаемый идентификатор';

comment on column dispatcher.dispatcher.last_name is 'Фамилия';

comment on column dispatcher.dispatcher.first_name is 'Имя';

comment on column dispatcher.dispatcher.patronymic is 'Отчество';

comment on column dispatcher.dispatcher.phone is 'Номер телефона';

comment on column dispatcher.dispatcher.email is 'E-Mail';

comment on column dispatcher.dispatcher.contractor_id is 'Контрагент';

comment on column dispatcher.dispatcher.active is 'Признак активности диспетчера';

comment on column dispatcher.dispatcher.consent is 'Флаг подписания ПДн';

create index dispatcher_dispatcher_active_idx
    on dispatcher.dispatcher (active);

create index dispatcher_dispatcher_contractor_idx
    on dispatcher.dispatcher (contractor_id);

create index dispatcher_dispatcher_email_idx
    on dispatcher.dispatcher (email);

create index dispatcher_dispatcher_id_email_idx
    on dispatcher.dispatcher (email, id);

create index dispatcher_dispatcher_id_phone_idx
    on dispatcher.dispatcher (phone, id);

create index dispatcher_dispatcher_phone_idx
    on dispatcher.dispatcher (phone);

-- Create table driver

create table dispatcher.driver
(
    id uuid not null
        constraint dispatcher_driver_pkey
            primary key,
    contractor_id uuid
        constraint dispatcher_driver_contractor_fk
            references dispatcher.contractor,
    last_name varchar(255) not null,
    first_name varchar(255) not null,
    patronymic varchar(255),
    passport varchar(11),
    is_active boolean default true not null,
    contact_phone_number varchar(50),
    rating integer,
    driver_license_number varchar,
    service_license_number varchar,
    experience varchar,
    humanreadableid varchar(128),
    email text,
    latitude double precision,
    longitude double precision,
    point_time timestamp,
    time_zone text,
    serving boolean default false not null,
    online boolean default false not null,
    active_trip_id uuid,
    shift_id uuid,
    cargo_licence_number text,
    driver_speciality text,
    consent boolean default false not null
);

comment on column dispatcher.driver.is_active is 'Водитель активен';

comment on column dispatcher.driver.contact_phone_number is 'Контактный телефон';

comment on column dispatcher.driver.rating is 'Рейтинг водителя';

comment on column dispatcher.driver.driver_license_number is 'Серия номер водительского удостоверения';

comment on column dispatcher.driver.service_license_number is 'Номер лицензии предоставления услуг';

comment on column dispatcher.driver.experience is 'Стаж вождения';

comment on column dispatcher.driver.humanreadableid is 'Водитель активен';

comment on column dispatcher.driver.email is 'Почта';

comment on column dispatcher.driver.latitude is 'широта';

comment on column dispatcher.driver.longitude is 'долгота';

comment on column dispatcher.driver.point_time is 'временная метка';

comment on column dispatcher.driver.time_zone is 'временная зона';

comment on column dispatcher.driver.serving is 'флаг обслуживания заявки';

comment on column dispatcher.driver.online is 'флаг онлайн';

comment on column dispatcher.driver.active_trip_id is 'идентификатор заявки';

comment on column dispatcher.driver.shift_id is 'Текущая смена';

comment on column dispatcher.driver.cargo_licence_number is 'Номер лиценции о предоставлении услуг по грузовым перевозкам';

comment on column dispatcher.driver.driver_speciality is 'Специализация водителя';

comment on column dispatcher.driver.consent is 'Флаг подписания ПДн';

create index dispatcher_driver_contractor_idx
    on dispatcher.driver (contractor_id);

create unique index dispatcher_driver_contractor_passport_uidx
    on dispatcher.driver (contractor_id, passport);

comment on index dispatcher.dispatcher_driver_contractor_passport_uidx is 'Уникальность водителя в рамках контрагента';

-- Create table driver_attribute

create table dispatcher.driver_attribute
(
    driver uuid,
    attribute uuid
);

comment on column dispatcher.driver_attribute.driver is 'Водитель';

comment on column dispatcher.driver_attribute.attribute is 'Признак водителя';

-- Create table driver_licenses

create table dispatcher.driver_licenses
(
    driver_id uuid
        constraint dispatcher_driver_licenses_driver_fk
            references dispatcher.driver,
    license text not null
);

comment on table dispatcher.driver_licenses is 'Права водителей';

comment on column dispatcher.driver_licenses.driver_id is 'Идентификатор водителя';

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

-- Create table shift

create table dispatcher.shift
(
    id uuid not null
        constraint dispatcher_shift_pk
            primary key,
    contractor_id uuid,
    driver_id uuid,
    vehicle_id uuid,
    start_date timestamp,
    end_date timestamp,
    is_deleted boolean default false not null,
    active boolean
);

comment on table dispatcher.shift is 'Cмены';

comment on column dispatcher.shift.id is 'Идентификатор';

comment on column dispatcher.shift.contractor_id is 'Идентификатор контрагента';

comment on column dispatcher.shift.driver_id is 'Идентификатор водителя';

comment on column dispatcher.shift.vehicle_id is 'Идентификатор автомобиля';

comment on column dispatcher.shift.start_date is 'Дата начала смены';

comment on column dispatcher.shift.end_date is 'Дата окончания смены';

comment on column dispatcher.shift.is_deleted is 'Признак удаленности смены';

comment on column dispatcher.shift.active is 'Признак активности смены';

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

-- Create table vehicle

create table dispatcher.vehicle
(
    id uuid not null
        constraint dispatcher_vehicle_pkey
            primary key,
    passport varchar(50),
    state_number varchar(50) not null,
    vin varchar(50),
    body_type varchar(255),
    chassis_type varchar(255),
    color varchar(255),
    eco_class varchar(255),
    fuel_consumption double precision,
    insurance_number varchar(50),
    manufacture_year integer
        constraint dispatcher_vehicle_manufacture_year_check
            check (manufacture_year >= 1900),
    max_allowed_weight integer,
    mileage integer,
    package_class varchar(255),
    transmission_type varchar(255),
    autopark_id uuid not null
        constraint dispatcher_vehicle_autopark_id_fk
            references dispatcher.autopark,
    engine_type varchar(255),
    in_exploitation boolean,
    active boolean default true,
    model_name text,
    model_brand text not null,
    model_year integer,
    vehicle_type text,
    vehicle_additional json
);

comment on table dispatcher.vehicle is 'Транспортные средства';

comment on column dispatcher.vehicle.id is 'Идентификатор ТС';

comment on column dispatcher.vehicle.passport is 'Паспортный номер';

comment on column dispatcher.vehicle.state_number is 'Автомобильный номер';

comment on column dispatcher.vehicle.vin is 'Идентификационный номер транспортного средства';

comment on column dispatcher.vehicle.body_type is 'Тип кузова';

comment on column dispatcher.vehicle.chassis_type is 'Тип привода';

comment on column dispatcher.vehicle.color is 'Цвет';

comment on column dispatcher.vehicle.eco_class is 'Класс Эко';

comment on column dispatcher.vehicle.fuel_consumption is 'Потребление топлива';

comment on column dispatcher.vehicle.insurance_number is 'Номер страховки';

comment on column dispatcher.vehicle.manufacture_year is 'Год производства';

comment on column dispatcher.vehicle.max_allowed_weight is 'Максимальный разрешенный вес';

comment on column dispatcher.vehicle.mileage is 'Пробег';

comment on column dispatcher.vehicle.package_class is 'Комплектация';

comment on column dispatcher.vehicle.transmission_type is 'Тип трансмиссии';

comment on column dispatcher.vehicle.autopark_id is 'Принадлежность к автопарку';

comment on column dispatcher.vehicle.engine_type is 'Тип двигателя';

comment on column dispatcher.vehicle.in_exploitation is 'В эксплуатации';

comment on column dispatcher.vehicle.active is 'Активен';

comment on column dispatcher.vehicle.model_name is 'Название модели';

comment on column dispatcher.vehicle.model_brand is 'Название марки';

comment on column dispatcher.vehicle.model_year is 'Год модели';

comment on column dispatcher.vehicle.vehicle_type is 'Тип автомобиля';

comment on column dispatcher.vehicle.vehicle_additional is 'Данные для грузовых автомобилей';

create index dispatcher_vehicle_autopark_idx
    on dispatcher.vehicle (autopark_id);














