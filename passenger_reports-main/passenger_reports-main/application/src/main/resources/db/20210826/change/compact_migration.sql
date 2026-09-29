create schema reports;
comment on schema reports is 'Отчеты';

create table reports.organization
(
    id            uuid not null
        constraint organization_pkey
            primary key,
    official_name varchar(255),
    address       varchar
);
comment on table reports.organization is 'Организации';
comment on column reports.organization.id is 'Идентификатор';
comment on column reports.organization.address is 'Адрес';
comment on column reports.organization.official_name is 'Название';

create table reports.address
(
    building                 varchar(255),
    city                     varchar(255),
    country                  varchar(255),
    house                    varchar(255),
    region                   varchar(255),
    street                   varchar(255),
    structure                varchar(255),
    id                       uuid not null
        constraint address_pkey
            primary key,
    exist_in_vsp_tb_registry boolean
);
comment on table reports.address is 'Адреса';
comment on column reports.address.building is 'Корпус';
comment on column reports.address.city is 'Город';
comment on column reports.address.country is 'Страна';
comment on column reports.address.house is 'Дом';
comment on column reports.address.region is 'Регион';
comment on column reports.address.street is 'Улица';
comment on column reports.address.structure is 'Строение';
comment on column reports.address.id is 'Идентификатор';
comment on column reports.address.exist_in_vsp_tb_registry is 'Адрес найден в реестре ВСП/ГОСБ/ТБ';

create table reports.position
(
    id            uuid not null
        constraint position_pkey
            primary key,
    position_name varchar(255)
);
comment on table reports.position is 'Должности';
comment on column reports.position.id is 'Идентификатор';
comment on column reports.position.position_name is 'Название';

create table reports.trip_purpose
(
    id           uuid                 not null
        constraint trip_purpose_pkey
            primary key,
    label        varchar(255),
    active       boolean default true not null,
    organization uuid
);
comment on table reports.trip_purpose is 'Цели поездки';
comment on column reports.trip_purpose.id is 'Идентификатор';
comment on column reports.trip_purpose.label is 'Название';
comment on column reports.trip_purpose.active is 'Активность';
comment on column reports.trip_purpose.organization is 'Организация';

create table reports.department
(
    id                uuid not null
        constraint department_pkey
            primary key,
    department_name   varchar(255),
    parent_id         uuid,
    organization_id   uuid,
    human_readable_id varchar
);
comment on table reports.department is 'Подразделение';
comment on column reports.department.id is 'Идентификатор';
comment on column reports.department.department_name is 'Название';
comment on column reports.department.parent_id is 'Родительский департамент';
comment on column reports.department.organization_id is 'Идентификатор оргранизацции';
comment on column reports.department.human_readable_id is 'Человекочитаемый идентификатор';

create table reports.employee
(
    id                          uuid not null
        constraint employee_pkey
            primary key,
    first_name                  varchar(255),
    last_name                   varchar(255),
    patronymic                  varchar(255),
    personnel_number            varchar(255),
    human_readable_id           varchar(100),
    mobile_phone                varchar(255),
    position_id                 uuid,
    department_id               uuid,
    organization_id             uuid,
    itinerant_type              varchar(10),
    cost_center                 varchar(255),
    marriage_certificate_number varchar(512)
);
comment on table reports.employee is 'Сотрудники';
comment on column reports.employee.id is 'Идентификатор';
comment on column reports.employee.first_name is 'Имя';
comment on column reports.employee.last_name is 'Фамилия';
comment on column reports.employee.patronymic is 'Отчество';
comment on column reports.employee.personnel_number is 'ТН';
comment on column reports.employee.human_readable_id is 'Человекочитаемый идентификатор';
comment on column reports.employee.mobile_phone is 'Номер теоефона';
comment on column reports.employee.position_id is 'Должность';
comment on column reports.employee.department_id is 'Подразделение';
comment on column reports.employee.organization_id is 'Идентификатор оргранизацции';
comment on column reports.employee.itinerant_type is 'Характер работы';
comment on column reports.employee.cost_center is 'Место возникновения затрат';
comment on column reports.employee.marriage_certificate_number is 'Номер свидетельства о браке';

create table reports.request
(
    id                                 uuid not null
        constraint request_pkey
            primary key,
    humanreadableid                    varchar(100)
        constraint request_humanreadableid_key
            unique,
    approved_by_id                     uuid,
    author_id                          uuid,
    passenger_id                       uuid,
    purpose_id                         uuid,
    creation_time                      timestamp,
    desired_date                       timestamp,
    finished_time                      timestamp,
    carrier                            varchar(255),
    transport_type                     varchar(255),
    trip_class                         varchar(255),
    approval_state                     varchar(255),
    approval_date                      timestamp,
    request_status                     varchar(255),
    limit_id                           uuid,
    tariff_id                          uuid,
    coop_trip                          boolean,
    passenger_count                    integer,
    savings                            double precision,
    actual_cost                        double precision,
    actual_distance                    double precision,
    actual_duration                    bigint,
    total_waiting_time                 bigint,
    additional_options                 varchar(2000),
    comment_for_driver                 varchar(255),
    rating_mark                        integer,
    rating_comment                     varchar(255),
    rating_advantages                  jsonb,
    rating_drawbacks                   jsonb,
    start_waypoint_id                  uuid,
    end_waypoint_id                    uuid,
    expected_cost                      double precision,
    expected_distance                  double precision,
    expected_time                      bigint,
    magenta_order_id                   bigint,
    shared_ride_id                     bigint,
    contractor_id                      uuid,
    carsharing_class                   varchar(255),
    personal_car                       uuid,
    public_compensation_document_exist boolean default false,
    request_status_code                integer,
    dispatcher_id                      uuid,
    deadline                           bigint,
    autopark_id                        uuid,
    driver_id                          uuid
);

comment on column reports.request.contractor_id is 'Каршеринговая компания';
comment on column reports.request.carsharing_class is 'Класс каршеринга';
comment on column reports.request.personal_car is 'Личный транспорт';
comment on column reports.request.public_compensation_document_exist is 'Есть ли вложение';
comment on column reports.request.request_status_code is 'Подстатус заявки';
comment on column reports.request.dispatcher_id is 'Идентификатор сотрудника диспетчера';
comment on column reports.request.deadline is 'Состояние контрольного срока, мин';
comment on column reports.request.autopark_id is 'Водитель';

create table reports.waypoint
(
    id             uuid not null
        constraint waypoint_pkey
            primary key,
    request_id     uuid,
    address_id     uuid,
    ordering_index integer,
    wait_time      bigint,
    constraint waypoint_uk
        unique (request_id, address_id, ordering_index)
);

create table reports.kpi
(
    id                uuid not null
        constraint kpi_pkey
            primary key,
    total_cost        double precision,
    total_distance_km double precision,
    total_time_min    integer
);

create table reports.order_kpi
(
    id                uuid not null
        constraint order_kpi_pkey
            primary key,
    cost_share_part   double precision,
    order_distance_km integer,
    order_id          bigint
        constraint order_kpi_order_id_check
            check (order_id >= 0),
    ride_time_min     integer,
    savings           double precision
        constraint order_kpi_savings_check
            check (savings >= (0)::double precision),
    savings_pct       double precision
        constraint order_kpi_savings_pct_check
            check (savings_pct >= (0)::double precision),
    kpi_id            uuid
);

create table reports.shared_ride
(
    magenta_id bigint               not null
        constraint shared_ride_pkey
            primary key,
    passengers integer
        constraint shared_ride_passengers_check
            check (passengers >= 1),
    active     boolean default true not null,
    tariff_id  uuid,
    kpi        uuid
);

create table reports.taxi_trip
(
    trip_type                 varchar(50) not null,
    id                        uuid        not null
        constraint taxi_trip_pkey
            primary key,
    date_time_registered      timestamp,
    organization_id           uuid        not null,
    tariff_id                 uuid        not null,
    trip_finish_time          timestamp,
    trip_start_time           timestamp,
    status                    varchar(255),
    taxi_id                   varchar(50),
    trip_fact_distance        double precision,
    trip_fact_duration        bigint,
    trip_fact_price           integer,
    trip_fact_wait_time       bigint,
    shared_request_id         integer,
    request_id                uuid,
    trip_assignment_date_time timestamp,
    car_brand_name            varchar,
    car_model                 varchar,
    car_color                 varchar,
    car_registration_number   varchar
);
comment on column reports.taxi_trip.trip_assignment_date_time is 'Дата и время назначения водителя на заявку';
comment on column reports.taxi_trip.car_brand_name is 'Брэнд автомобиля';
comment on column reports.taxi_trip.car_model is 'Модель автомобиля';
comment on column reports.taxi_trip.car_color is 'Цвет автомобиля';
comment on column reports.taxi_trip.car_registration_number is 'Номер автомобиля';

create table reports.tariff
(
    id                                            uuid not null
        constraint tariff_pkey
            primary key,
    organization_id                               uuid,
    transport_type                                varchar(50),
    humanreadableid                               varchar(100),
    service_type                                  varchar(255),
    region                                        varchar(255),
    active                                        boolean          default true,
    car_service_cost                              integer,
    ride_cost_per_km                              integer,
    ride_cost_per_min                             integer,
    taxi_class                                    varchar(255),
    wait_cost_per_min                             integer,
    wait_cost_per_min_intermediate                integer          default 0,
    min_ride_distance_cost                        integer          default 0,
    min_ride_time_cost                            integer          default 0,
    contractor_max_diff_computed_distance_percent smallint
        constraint tariff_contractor_max_diff_computed_distance_percent_check
            check ((contractor_max_diff_computed_distance_percent >= 0) AND
                   (contractor_max_diff_computed_distance_percent <= 100)),
    contractor_max_diff_fact_distance_percent     smallint
        constraint tariff_contractor_max_diff_fact_distance_percent_check
            check ((contractor_max_diff_fact_distance_percent >= 0) AND
                   (contractor_max_diff_fact_distance_percent <= 100)),
    contractor_max_diff_computed_cost_percent     smallint
        constraint tariff_contractor_max_diff_computed_cost_percent_check
            check ((contractor_max_diff_computed_cost_percent >= 0) AND
                   (contractor_max_diff_computed_cost_percent <= 100)),
    contractor_max_diff_contractor_cost_percent   smallint
        constraint tariff_contractor_max_diff_contractor_cost_percent_check
            check ((contractor_max_diff_contractor_cost_percent >= 0) AND
                   (contractor_max_diff_contractor_cost_percent <= 100)),
    contractor_max_diff_computed_waiting_percent  smallint
        constraint tariff_contractor_max_diff_computed_waiting_percent_check
            check ((contractor_max_diff_computed_waiting_percent >= 0) AND
                   (contractor_max_diff_computed_waiting_percent <= 100)),
    contract_id                                   uuid,
    coef_traffic                                  double precision default 0,
    coef_child_seat                               double precision default 1,
    coef_pet_transport                            double precision default 1,
    coef_casco                                    double precision default 1,
    coef_work_day_morning                         double precision default 1,
    coef_work_day_noon                            double precision default 1,
    coef_work_day_evening                         double precision default 1,
    coef_work_day_night                           double precision default 1,
    coef_day_off                                  double precision default 1
);
comment on column reports.tariff.coef_traffic is 'Коэффициент загрузки дорог (пробок, баллы Яндекс, прогнозные) Более 7 баллов';
comment on column reports.tariff.coef_child_seat is 'Коэффициент доплаты за детское кресло';
comment on column reports.tariff.coef_pet_transport is 'Коэффициент доплаты за перевозку животного';
comment on column reports.tariff.coef_casco is 'Коэффициент на полное покрытие ответственности КАСКО';
comment on column reports.tariff.coef_work_day_morning is 'Коэффициент временного интервала поездки: утро будние дни 07:00-10:00';
comment on column reports.tariff.coef_work_day_noon is 'Коэффициент временного интервала поездки: день будние дни 10:00-18:00';
comment on column reports.tariff.coef_work_day_evening is 'Коэффициент временного интервала поездки: вечерний будние дни 18:00-22:00';
comment on column reports.tariff.coef_work_day_night is 'Коэффициент временного интервала поездки: ночной будние дни 22:00-07:00';
comment on column reports.tariff.coef_day_off is 'Коэффициент выходного дня: СБ, ВСКР';

create table reports.contractor
(
    id   uuid not null
        constraint contractor_pkey
            primary key,
    name varchar(255)
);

create table reports.taxi_trip_registry
(
    id                    uuid                  not null
        constraint taxi_trip_registry_pkey
            primary key,
    date                  date                  not null,
    contractor_id         uuid,
    is_valid              boolean default false not null,
    upload_file_full_name varchar(200),
    constraint date_contractor_unique
        unique (contractor_id, date)
);
comment on column reports.taxi_trip_registry.upload_file_full_name is 'Полное имя файла';

create table reports.blank_cell_address
(
    registry_id uuid
        constraint blank_cell_address_registry_id_fkey
            references reports.taxi_trip_registry,
    blank_cell  varchar(255)
);

create table reports.incorrect_type_cell_address
(
    registry_id    uuid
        constraint incorrect_type_cell_address_registry_id_fkey
            references reports.taxi_trip_registry,
    incorrect_cell varchar(255)
);

create table reports.personal_auto
(
    id                uuid not null
        constraint personal_auto_pkey
            primary key,
    brand_name        varchar(255),
    engine_volume     integer,
    insurance_number  varchar(255),
    model             varchar(255),
    owner_info        varchar(255),
    reg_cert          varchar(255),
    reg_number        varchar(255),
    corporate_user_id uuid
);

create table reports.taxi_trip_registry_string
(
    id                         uuid                  not null
        constraint taxi_trip_registry_string_pkey
            primary key,
    registry_id                uuid
        constraint taxi_trip_registry_string_registry_id_fkey
            references reports.taxi_trip_registry,
    calc_trip_status           varchar(255),
    calc_trip_type             varchar(255),
    ordinal                    integer,
    taxi_id                    varchar(50),
    department_mvz             varchar(255),
    desired_time               timestamp,
    desired_date               timestamp,
    taxi_class                 varchar(255),
    start_address              text,
    intermediate_address       text,
    finish_address             text,
    fact_time                  timestamp,
    fact_start_time            timestamp,
    fact_finish_time           timestamp,
    fact_wait_time             integer,
    fact_distance_km           double precision,
    fact_min_cost_rub          double precision,
    fact_wait_cost_rub_per_min double precision,
    fact_price_rub_per_km      double precision,
    sum_without_nds_rub        double precision,
    nds_rub                    double precision,
    sum_rub                    double precision,
    valid_taxi_id              boolean default false not null,
    valid_trip_status          boolean default false not null,
    valid_trip_date            boolean default false not null,
    valid_cancelled_trip_cost  boolean default false not null,
    valid_calc_distance        boolean default false not null,
    valid_fact_distance        boolean default false not null,
    valid_tariff               boolean default false not null,
    valid_calc_cost            boolean default false not null,
    valid_fact_cost            boolean default false not null,
    valid_wait_time            boolean default false not null
);
comment on table reports.taxi_trip_registry_string is 'Таблица-шаблон для строк Реестра поездок на такси от контрагентов';
comment on column reports.taxi_trip_registry_string.registry_id is 'ID реестра';
comment on column reports.taxi_trip_registry_string.ordinal is 'Порядковый № строки';
comment on column reports.taxi_trip_registry_string.taxi_id is 'ID поездки в системе контрагента';
comment on column reports.taxi_trip_registry_string.department_mvz is 'Подразделение заказчика (код МВЗ)';
comment on column reports.taxi_trip_registry_string.desired_time is 'Желаемое время подачи автомобиля';
comment on column reports.taxi_trip_registry_string.desired_date is 'Желаемая дата подачи автомобиля (дубль колонки)';
comment on column reports.taxi_trip_registry_string.taxi_class is 'Вид тарифа';
comment on column reports.taxi_trip_registry_string.start_address is 'Адрес подачи ТС';
comment on column reports.taxi_trip_registry_string.intermediate_address is 'Адреса промежуточных точек маршрута';
comment on column reports.taxi_trip_registry_string.finish_address is 'Адрес конечного пункта';
comment on column reports.taxi_trip_registry_string.fact_time is 'Фактическое время подачи ТС';
comment on column reports.taxi_trip_registry_string.fact_start_time is 'Фактическое время начала поездки';
comment on column reports.taxi_trip_registry_string.fact_finish_time is 'Фактическое время окончания поездки';
comment on column reports.taxi_trip_registry_string.fact_wait_time is 'Фактическое время ожидания (простоя), мин';
comment on column reports.taxi_trip_registry_string.fact_distance_km is 'Фактический километраж поездки, км';
comment on column reports.taxi_trip_registry_string.fact_min_cost_rub is 'Стоимость минимальной поездки поездки (без НДС), руб';
comment on column reports.taxi_trip_registry_string.fact_wait_cost_rub_per_min is 'Стоимость ожидания (без НДС), руб/мин';
comment on column reports.taxi_trip_registry_string.fact_price_rub_per_km is 'Тариф (без НДС), руб/км';
comment on column reports.taxi_trip_registry_string.sum_without_nds_rub is 'Итого (без НДС), руб';
comment on column reports.taxi_trip_registry_string.nds_rub is 'Итого НДC, руб';
comment on column reports.taxi_trip_registry_string.sum_rub is 'Итого c НДC, руб';
comment on column reports.taxi_trip_registry_string.valid_taxi_id is 'Проверка 0 - Проверка совпадения ID поездки';
comment on column reports.taxi_trip_registry_string.valid_trip_status is 'Проверка 1 - Проверка статуса поездки';
comment on column reports.taxi_trip_registry_string.valid_trip_date is 'Проверка 2 - Проверка даты поездки';
comment on column reports.taxi_trip_registry_string.valid_cancelled_trip_cost is 'Проверка 3 - Проверка стоимость = 0 для отмененной поездки';
comment on column reports.taxi_trip_registry_string.valid_calc_distance is 'Проверка 4 - Сравнение протяженности маршрута с расчетной';
comment on column reports.taxi_trip_registry_string.valid_fact_distance is 'Проверка 4a - Сравнение протяженности маршрута с фактической';
comment on column reports.taxi_trip_registry_string.valid_tariff is 'Проверка 5 - Проверка тарифа';
comment on column reports.taxi_trip_registry_string.valid_calc_cost is 'Проверка 6 - Сравнение стоимости поездки с расчетной';
comment on column reports.taxi_trip_registry_string.valid_fact_cost is 'Проверка 7 - Сравнение стоимости поездки с фактической';
comment on column reports.taxi_trip_registry_string.valid_wait_time is 'Проверка 8 - Проверка времени ожидания';

create table reports.contract
(
    id            uuid not null
        constraint contract_pkey
            primary key,
    contractor_id uuid,
    active        boolean default true
);
comment on table reports.contract is 'Контракты из модуля тарифов';
comment on column reports.contract.id is 'Идентификатор контракта';
comment on column reports.contract.contractor_id is 'Идентификатор контрагента';
comment on column reports.contract.active is 'Флаг активности';

create table reports.driver
(
    id                              uuid not null
        constraint driver_pkey
            primary key,
    active                          boolean,
    autopark_id                     uuid,
    contact_phone                   varchar(255),
    contractor_id                   uuid,
    driver_license_number           varchar(255),
    experience                      varchar(255),
    first_name                      varchar(255),
    last_name                       varchar(255),
    passport                        varchar(255),
    patronymic                      varchar(255),
    rating                          integer,
    service_provider_license_number varchar(255)
);
comment on table reports.driver is 'Водители';
comment on column reports.driver.id is 'Уникальный идентификатор';
comment on column reports.driver.active is 'Активность водителя';
comment on column reports.driver.autopark_id is 'Идентификатор автопарка';
comment on column reports.driver.contact_phone is 'Контактный номер телефона';
comment on column reports.driver.contractor_id is 'Идентификатор контрагента';
comment on column reports.driver.driver_license_number is 'Номер ВУ';
comment on column reports.driver.experience is 'Опыт';
comment on column reports.driver.first_name is 'Имя';
comment on column reports.driver.last_name is 'Фамилия';
comment on column reports.driver.passport is 'Серия и номер пасспорта';
comment on column reports.driver.patronymic is 'Отчество';
comment on column reports.driver.rating is 'Рейтинг водителя';
comment on column reports.driver.service_provider_license_number is 'Номер провайдера лицензии';

create table reports.driver_tag
(
    id            uuid not null
        constraint driver_tag_pkey
            primary key,
    contractor_id uuid,
    name          varchar(255)
);
comment on table reports.driver_tag is 'Таблица признаков водителей';
comment on column reports.driver_tag.id is 'Уникальный идентификатор';
comment on column reports.driver_tag.contractor_id is 'Идентификатор контрагента';
comment on column reports.driver_tag.name is 'Имя признака';

create table reports.driver_tag_connector
(
    driver_id uuid not null,
    tag_id    uuid not null,
    constraint driver_tag_connector_pkey
        primary key (driver_id, tag_id)
);
comment on table reports.driver_tag_connector is 'Соединительная таблица для водителей и их признаков';
comment on column reports.driver_tag_connector.driver_id is 'Идентификатор водителя';
comment on column reports.driver_tag_connector.tag_id is 'Идентификатор признака';

create table reports.driver_license_classes
(
    driver_id uuid not null
        constraint driver_license_classes_driver_id_fkey
            references reports.driver,
    class     varchar(255)
);
comment on table reports.driver_license_classes is 'Классы прав';
comment on column reports.driver_license_classes.driver_id is 'Идентификатор водителя';
comment on column reports.driver_license_classes.class is 'Категория ВУ';

create table reports.autopark
(
    id            uuid not null
        constraint autopark_pkey
            primary key,
    autopark_name varchar(255)
);
comment on table reports.autopark is 'Автопарки';
comment on column reports.autopark.id is 'Уникальный идентификатор';
comment on column reports.autopark.autopark_name is 'Название автопарка';

create table reports."limit"
(
    id                   uuid not null
        constraint limit_pkey
            primary key,
    department_limit_id  uuid,
    human_readable_id    varchar(100),
    limit_status         varchar(100),
    year                 integer,
    limit_type           varchar(100),
    transport_type       varchar(100),
    limit_sharing_type   varchar(100),
    limit_service_type   varchar(100),
    sum                  bigint,
    balance              bigint,
    reserve              bigint,
    creation_time        timestamp,
    parent_id            uuid,
    organization_id      uuid,
    department_id        uuid,
    employee_id          uuid,
    parent_department_id uuid,
    economy              bigint
);
comment on table reports."limit" is 'Таблица лимитов';
comment on column reports."limit".id is 'Идентификатор лимита';
comment on column reports."limit".human_readable_id is 'Человекочитаемый идентификатор';
comment on column reports."limit".limit_status is 'Статус лимита PLANNING\SHARED\CLOSED';
comment on column reports."limit".year is 'Год лимита';
comment on column reports."limit".limit_type is 'Тип лимита DEPARTMENT\EMPLOYEE';
comment on column reports."limit".transport_type is 'Тип транспорта лимита PUBLIC\PERSONAL\TAXI';
comment on column reports."limit".limit_sharing_type is 'Тип распределения лимита MONTHLY\QUARTER\PERCENTS';
comment on column reports."limit".limit_service_type is 'Тип обслуживания PASSENGER/CARGO';
comment on column reports."limit".sum is 'Общая годовая сумма лимита';
comment on column reports."limit".balance is 'Баланс лимита(Для типов транспорта)';
comment on column reports."limit".reserve is 'Резерв лимита(Для подразделений)';
comment on column reports."limit".creation_time is 'Дата создания лимита';
comment on column reports."limit".parent_id is 'Идентификатор родительского лимита';
comment on column reports."limit".organization_id is 'Идентификатор организации владеющей лимитом';
comment on column reports."limit".department_id is 'Идентификатор подразделения владеющего лимитом';
comment on column reports."limit".employee_id is 'Идентификатор сотрудника который владеет лимитом';
comment on column reports."limit".parent_department_id is 'Идентификатор родительского подразделения';
comment on column reports."limit".economy is 'Экономия';

create table reports.request_status_overdue_message
(
    id                             uuid,
    request_id                     uuid,
    trip_request_status            varchar,
    carsharing_join_request_status varchar,
    deadline_chrono_unit           varchar,
    deadline_value                 smallint,
    overdue_time                   timestamp
);
comment on table reports.request_status_overdue_message is 'Просроченные по КС заявки';
comment on column reports.request_status_overdue_message.id is 'ID';
comment on column reports.request_status_overdue_message.request_id is 'ID заявки';
comment on column reports.request_status_overdue_message.trip_request_status is 'Статус заявки (на поездку)';
comment on column reports.request_status_overdue_message.carsharing_join_request_status is 'Статус заявки (на подключение к копр. каршерингу)';
comment on column reports.request_status_overdue_message.deadline_chrono_unit is 'Единица измерения времени КС';
comment on column reports.request_status_overdue_message.deadline_value is 'Значение КС';
comment on column reports.request_status_overdue_message.overdue_time is 'Время, в которое был превышен КС';

create table reports.transport_compensation
(
    id                       uuid         not null
        constraint transport_compensation_pkey
            primary key,
    compensation_type        varchar(255) not null,
    transport_type           varchar(255) not null,
    tickets_cost             varchar(255) not null,
    tickets_count            varchar(255) not null,
    tickets_expiration_start date,
    tickets_expiration_end   date,
    request_id               uuid
        constraint transport_compensation_request_id_fkey
            references reports.request,
    attached_document_id     uuid
);
comment on column reports.transport_compensation.compensation_type is 'Тип компенсации';
comment on column reports.transport_compensation.transport_type is 'Тип транспорта';
comment on column reports.transport_compensation.tickets_cost is 'Стоимость билета';
comment on column reports.transport_compensation.tickets_count is 'Кол-во билетов';
comment on column reports.transport_compensation.tickets_expiration_start is 'Дата начала действия билета';
comment on column reports.transport_compensation.tickets_expiration_end is 'Дата окончания действия билета';
comment on column reports.transport_compensation.request_id is 'Поездка';
comment on column reports.transport_compensation.attached_document_id is 'Прикрепленный документ';