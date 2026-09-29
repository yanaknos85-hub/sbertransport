-- DROP SCHEMA oto_cargo;
CREATE SCHEMA oto_cargo;

COMMENT ON SCHEMA oto_cargo IS 'Исполнение грузовых заявок';

-- oto_cargo.address definition

-- Drop table

-- DROP TABLE oto_cargo.address;

CREATE TABLE oto_cargo.address (
    building varchar(255) NULL, -- Корпус
    city varchar(255) NULL, -- Город
    country varchar(255) NULL, -- Страна
    house varchar(255) NULL, -- Дом
    region varchar(255) NULL, -- Регион
    street varchar(255) NULL, -- Улица
    "structure" varchar(255) NULL, -- Строение
    id uuid NOT NULL, -- Идентификатор
    exist_in_vsp_tb_registry bool NULL, -- Адрес найден в реестре ВСП/ГОСБ/ТБ
    address_string varchar(255) NULL,
    CONSTRAINT address_pkey PRIMARY KEY (id)
);
COMMENT ON TABLE oto_cargo.address IS 'Адреса';

-- Column comments

COMMENT ON COLUMN oto_cargo.address.building IS 'Корпус';
COMMENT ON COLUMN oto_cargo.address.city IS 'Город';
COMMENT ON COLUMN oto_cargo.address.country IS 'Страна';
COMMENT ON COLUMN oto_cargo.address.house IS 'Дом';
COMMENT ON COLUMN oto_cargo.address.region IS 'Регион';
COMMENT ON COLUMN oto_cargo.address.street IS 'Улица';
COMMENT ON COLUMN oto_cargo.address."structure" IS 'Строение';
COMMENT ON COLUMN oto_cargo.address.id IS 'Идентификатор';
COMMENT ON COLUMN oto_cargo.address.exist_in_vsp_tb_registry IS 'Адрес найден в реестре ВСП/ГОСБ/ТБ';



-- oto_cargo.contract definition

-- Drop table

-- DROP TABLE oto_cargo.contract;

CREATE TABLE oto_cargo.contract (
    id uuid NOT NULL, -- Идентификатор контракта
    contractor_id uuid NULL, -- Идентификатор контрагента
    active bool NULL DEFAULT true, -- Флаг активности
    CONSTRAINT contract_pkey PRIMARY KEY (id)
);
COMMENT ON TABLE oto_cargo.contract IS 'Контракты из модуля тарифов';

-- Column comments

COMMENT ON COLUMN oto_cargo.contract.id IS 'Идентификатор контракта';
COMMENT ON COLUMN oto_cargo.contract.contractor_id IS 'Идентификатор контрагента';
COMMENT ON COLUMN oto_cargo.contract.active IS 'Флаг активности';


-- oto_cargo.contractor definition

-- Drop table

-- DROP TABLE oto_cargo.contractor;

CREATE TABLE oto_cargo.contractor (
    id uuid NOT NULL,
    "name" varchar(255) NULL,
    active bool NULL DEFAULT true, -- Признак активности контрагента
    CONSTRAINT contractor_pkey PRIMARY KEY (id)
);

-- Column comments

COMMENT ON COLUMN oto_cargo.contractor.active IS 'Признак активности контрагента';


-- oto_cargo.department definition

-- Drop table

-- DROP TABLE oto_cargo.department;

CREATE TABLE oto_cargo.department (
    id uuid NOT NULL, -- Идентификатор
    department_name varchar(255) NULL, -- Название
    parent_id uuid NULL, -- Родительский департамент
    organization_id uuid NULL, -- Идентификатор оргранизацции
    human_readable_id varchar NULL, -- Человекочитаемый идентификатор
    CONSTRAINT department_pkey PRIMARY KEY (id)
);
CREATE INDEX department_organization_id_idx_manual ON oto_cargo.department USING btree (organization_id);
CREATE INDEX oto_department_organization_id_idx ON oto_cargo.department USING btree (organization_id);
CREATE INDEX oto_department_organization_test_idx ON oto_cargo.department USING btree (organization_id);
COMMENT ON TABLE oto_cargo.department IS 'Подразделение';

-- Column comments

COMMENT ON COLUMN oto_cargo.department.id IS 'Идентификатор';
COMMENT ON COLUMN oto_cargo.department.department_name IS 'Название';
COMMENT ON COLUMN oto_cargo.department.parent_id IS 'Родительский департамент';
COMMENT ON COLUMN oto_cargo.department.organization_id IS 'Идентификатор оргранизацции';
COMMENT ON COLUMN oto_cargo.department.human_readable_id IS 'Человекочитаемый идентификатор';


-- oto_cargo.employee definition

-- Drop table

-- DROP TABLE oto_cargo.employee;

CREATE TABLE oto_cargo.employee (
    id uuid NOT NULL, -- Идентификатор
    first_name varchar(255) NULL, -- Имя
    last_name varchar(255) NULL, -- Фамилия
    patronymic varchar(255) NULL, -- Отчество
    personnel_number varchar(255) NULL, -- ТН
    human_readable_id varchar(100) NULL, -- Человекочитаемый идентификатор
    mobile_phone varchar(255) NULL, -- Номер теоефона
    position_id uuid NULL, -- Должность
    department_id uuid NULL, -- Подразделение
    organization_id uuid NULL, -- Идентификатор оргранизацции
    itinerant_type varchar(10) NULL, -- Характер работы
    cost_center varchar(255) NULL, -- Место возникновения затрат
    marriage_certificate_number varchar(512) NULL, -- Номер свидетельства о браке
    user_id uuid NULL, -- id of linked user
    CONSTRAINT employee_pkey PRIMARY KEY (id)
);
CREATE INDEX employee_department_id_idx ON oto_cargo.employee USING btree (department_id);
CREATE INDEX employee_department_id_idx_manual ON oto_cargo.employee USING btree (department_id);
CREATE INDEX oto_employee_department_id_idx ON oto_cargo.employee USING btree (department_id);
COMMENT ON TABLE oto_cargo.employee IS 'Сотрудники';

-- Column comments

COMMENT ON COLUMN oto_cargo.employee.id IS 'Идентификатор';
COMMENT ON COLUMN oto_cargo.employee.first_name IS 'Имя';
COMMENT ON COLUMN oto_cargo.employee.last_name IS 'Фамилия';
COMMENT ON COLUMN oto_cargo.employee.patronymic IS 'Отчество';
COMMENT ON COLUMN oto_cargo.employee.personnel_number IS 'ТН';
COMMENT ON COLUMN oto_cargo.employee.human_readable_id IS 'Человекочитаемый идентификатор';
COMMENT ON COLUMN oto_cargo.employee.mobile_phone IS 'Номер теоефона';
COMMENT ON COLUMN oto_cargo.employee.position_id IS 'Должность';
COMMENT ON COLUMN oto_cargo.employee.department_id IS 'Подразделение';
COMMENT ON COLUMN oto_cargo.employee.organization_id IS 'Идентификатор оргранизацции';
COMMENT ON COLUMN oto_cargo.employee.itinerant_type IS 'Характер работы';
COMMENT ON COLUMN oto_cargo.employee.cost_center IS 'Место возникновения затрат';
COMMENT ON COLUMN oto_cargo.employee.marriage_certificate_number IS 'Номер свидетельства о браке';
COMMENT ON COLUMN oto_cargo.employee.user_id IS 'id of linked user';


-- oto_cargo.evaluation definition

-- Drop table

-- DROP TABLE oto_cargo.evaluation;

CREATE TABLE oto_cargo.evaluation (
    id uuid NOT NULL, -- Идентификатор
    request_id uuid NOT NULL, -- Id заявки
    rating int4 NOT NULL, -- Рейтинг оценки (от 1 до 5)
    "comment" varchar(255) NULL, -- Комментарий к оценке
    CONSTRAINT evaluation_pk PRIMARY KEY (id)
);
CREATE INDEX evaluation_request_id_idx_manual ON oto_cargo.evaluation USING btree (request_id);
CREATE INDEX oto_evaluation_request_id_idx ON oto_cargo.evaluation USING btree (request_id);
COMMENT ON TABLE oto_cargo.evaluation IS 'Оценка заявки';

-- Column comments

COMMENT ON COLUMN oto_cargo.evaluation.id IS 'Идентификатор';
COMMENT ON COLUMN oto_cargo.evaluation.request_id IS 'Id заявки';
COMMENT ON COLUMN oto_cargo.evaluation.rating IS 'Рейтинг оценки (от 1 до 5)';
COMMENT ON COLUMN oto_cargo.evaluation."comment" IS 'Комментарий к оценке';


-- oto_cargo.evaluation_reasons definition

-- Drop table

-- DROP TABLE oto_cargo.evaluation_reasons;

CREATE TABLE oto_cargo.evaluation_reasons (
    evaluation_id uuid NOT NULL, -- Id причины
    reason varchar(50) NOT NULL, -- Название причины
    CONSTRAINT evaluation_reasons_pkey PRIMARY KEY (evaluation_id, reason)
);
CREATE INDEX evaluation_reasons_evaluation_id_idx_manual ON oto_cargo.evaluation_reasons USING btree (evaluation_id);
COMMENT ON TABLE oto_cargo.evaluation_reasons IS 'Оценка заявки, причины';

-- Column comments

COMMENT ON COLUMN oto_cargo.evaluation_reasons.evaluation_id IS 'Id причины';
COMMENT ON COLUMN oto_cargo.evaluation_reasons.reason IS 'Название причины';


-- oto_cargo.organization_group definition

-- Drop table

-- DROP TABLE oto_cargo.organization_group;

CREATE TABLE oto_cargo.organization_group (
    id uuid NOT NULL, -- Идентификатор
    "name" varchar(255) NOT NULL, -- Наименование группы организаций
    internal bool NOT NULL, -- Принадлежность к внутренней группе компаний
    CONSTRAINT organization_group_pk PRIMARY KEY (id)
);
COMMENT ON TABLE oto_cargo.organization_group IS 'Группы организаций';

-- Column comments

COMMENT ON COLUMN oto_cargo.organization_group.id IS 'Идентификатор';
COMMENT ON COLUMN oto_cargo.organization_group."name" IS 'Наименование группы организаций';
COMMENT ON COLUMN oto_cargo.organization_group.internal IS 'Принадлежность к внутренней группе компаний';


-- oto_cargo."position" definition

-- Drop table

-- DROP TABLE oto_cargo."position";

CREATE TABLE oto_cargo."position" (
    id uuid NOT NULL, -- Идентификатор
    position_name varchar(255) NULL, -- Название
    CONSTRAINT position_pkey PRIMARY KEY (id)
);
COMMENT ON TABLE oto_cargo."position" IS 'Должности';

-- Column comments

COMMENT ON COLUMN oto_cargo."position".id IS 'Идентификатор';
COMMENT ON COLUMN oto_cargo."position".position_name IS 'Название';


-- oto_cargo.routelist definition

-- Drop table

-- DROP TABLE oto_cargo.routelist;

CREATE TABLE oto_cargo.routelist (
    id uuid NOT NULL, -- ID
    humanreadableid varchar(100) NOT NULL, -- Человекочитаемый идентификатор
    tariff_id uuid NULL, -- Тариф
    contractor_info jsonb NULL, -- Данные по контрагенту
    "status" varchar(150) NULL, -- Статус
    status_code int4 NULL, -- Код статуса
    "cost" int8 NULL, -- Стоимость, руб
    distance float8 NULL, -- Дистанция, км
    active bool NOT NULL DEFAULT true, -- Флаг активности
    CONSTRAINT routelist_humanreadableid_key UNIQUE (humanreadableid),
    CONSTRAINT routelist_pkey PRIMARY KEY (id)
);

-- Column comments

COMMENT ON COLUMN oto_cargo.routelist.id IS 'ID';
COMMENT ON COLUMN oto_cargo.routelist.humanreadableid IS 'Человекочитаемый идентификатор';
COMMENT ON COLUMN oto_cargo.routelist.tariff_id IS 'Тариф';
COMMENT ON COLUMN oto_cargo.routelist.contractor_info IS 'Данные по контрагенту';
COMMENT ON COLUMN oto_cargo.routelist."status" IS 'Статус';
COMMENT ON COLUMN oto_cargo.routelist.status_code IS 'Код статуса';
COMMENT ON COLUMN oto_cargo.routelist."cost" IS 'Стоимость, руб';
COMMENT ON COLUMN oto_cargo.routelist.distance IS 'Дистанция, км';
COMMENT ON COLUMN oto_cargo.routelist.active IS 'Флаг активности';


-- oto_cargo."tariff" definition

-- Drop table

-- DROP TABLE oto_cargo."tariff";

CREATE TABLE oto_cargo."tariff" (
    id uuid NOT NULL,
    organization_id uuid NULL,
    "transport_type" varchar(50) NULL,
    humanreadableid varchar(100) NULL,
    "service_type" varchar(255) NULL,
    region varchar(255) NULL,
    active bool NULL DEFAULT true,
    car_service_cost int4 NULL,
    ride_cost_per_km int4 NULL,
    ride_cost_per_min int4 NULL,
    taxi_class varchar(255) NULL,
    wait_cost_per_min int4 NULL,
    wait_cost_per_min_intermediate int4 NULL DEFAULT 0,
    min_ride_distance_cost int4 NULL DEFAULT 0,
    min_ride_time_cost int4 NULL DEFAULT 0,
    contractor_max_diff_computed_distance_percent int2 NULL,
    contractor_max_diff_fact_distance_percent int2 NULL,
    contractor_max_diff_computed_cost_percent int2 NULL,
    contractor_max_diff_contractor_cost_percent int2 NULL,
    contractor_max_diff_computed_waiting_percent int2 NULL,
    contract_id uuid NULL,
    coef_traffic float8 NULL DEFAULT 0, -- Коэффициент загрузки дорог (пробок, баллы Яндекс, прогнозные) Более 7 баллов
    coef_child_seat float8 NULL DEFAULT 1, -- Коэффициент доплаты за детское кресло
    coef_pet_transport float8 NULL DEFAULT 1, -- Коэффициент доплаты за перевозку животного
    coef_casco float8 NULL DEFAULT 1, -- Коэффициент на полное покрытие ответственности КАСКО
    coef_work_day_morning float8 NULL DEFAULT 1, -- Коэффициент временного интервала поездки: утро будние дни 07:00-10:00
    coef_work_day_noon float8 NULL DEFAULT 1, -- Коэффициент временного интервала поездки: день будние дни 10:00-18:00
    coef_work_day_evening float8 NULL DEFAULT 1, -- Коэффициент временного интервала поездки: вечерний будние дни 18:00-22:00
    coef_work_day_night float8 NULL DEFAULT 1, -- Коэффициент временного интервала поездки: ночной будние дни 22:00-07:00
    coef_day_off float8 NULL DEFAULT 1, -- Коэффициент выходного дня: СБ, ВСКР
    CONSTRAINT tariff_contractor_max_diff_computed_cost_percent_check CHECK (((contractor_max_diff_computed_cost_percent >= 0) AND (contractor_max_diff_computed_cost_percent <= 100))),
    CONSTRAINT tariff_contractor_max_diff_computed_distance_percent_check CHECK (((contractor_max_diff_computed_distance_percent >= 0) AND (contractor_max_diff_computed_distance_percent <= 100))),
    CONSTRAINT tariff_contractor_max_diff_computed_waiting_percent_check CHECK (((contractor_max_diff_computed_waiting_percent >= 0) AND (contractor_max_diff_computed_waiting_percent <= 100))),
    CONSTRAINT tariff_contractor_max_diff_contractor_cost_percent_check CHECK (((contractor_max_diff_contractor_cost_percent >= 0) AND (contractor_max_diff_contractor_cost_percent <= 100))),
    CONSTRAINT tariff_contractor_max_diff_fact_distance_percent_check CHECK (((contractor_max_diff_fact_distance_percent >= 0) AND (contractor_max_diff_fact_distance_percent <= 100))),
    CONSTRAINT tariff_pkey PRIMARY KEY (id)
);

-- Column comments

COMMENT ON COLUMN oto_cargo."tariff".coef_traffic IS 'Коэффициент загрузки дорог (пробок, баллы Яндекс, прогнозные) Более 7 баллов';
COMMENT ON COLUMN oto_cargo."tariff".coef_child_seat IS 'Коэффициент доплаты за детское кресло';
COMMENT ON COLUMN oto_cargo."tariff".coef_pet_transport IS 'Коэффициент доплаты за перевозку животного';
COMMENT ON COLUMN oto_cargo."tariff".coef_casco IS 'Коэффициент на полное покрытие ответственности КАСКО';
COMMENT ON COLUMN oto_cargo."tariff".coef_work_day_morning IS 'Коэффициент временного интервала поездки: утро будние дни 07:00-10:00';
COMMENT ON COLUMN oto_cargo."tariff".coef_work_day_noon IS 'Коэффициент временного интервала поездки: день будние дни 10:00-18:00';
COMMENT ON COLUMN oto_cargo."tariff".coef_work_day_evening IS 'Коэффициент временного интервала поездки: вечерний будние дни 18:00-22:00';
COMMENT ON COLUMN oto_cargo."tariff".coef_work_day_night IS 'Коэффициент временного интервала поездки: ночной будние дни 22:00-07:00';
COMMENT ON COLUMN oto_cargo."tariff".coef_day_off IS 'Коэффициент выходного дня: СБ, ВСКР';



-- oto_cargo.template_for_cargo definition

-- Drop table

-- DROP TABLE oto_cargo.template_for_cargo;

CREATE TABLE oto_cargo.template_for_cargo (
    id uuid NOT NULL,
    humanreadableid varchar(255) NULL,
    "transport_type" varchar(255) NULL,
    creation_time timestamp NULL,
    cron_expression varchar(255) NULL,
    "status" varchar(255) NULL,
    requests_date_delivery jsonb NULL,
    "template" jsonb NULL,
    total_cost int8 NULL,
    organization_id uuid NULL,
    recipient_name varchar(200) NULL,
    sender_name varchar(200) NULL,
    recipient_address varchar(2000) NULL,
    sender_address varchar(2000) NULL,
    author_id uuid NULL,
    CONSTRAINT template_for_cargo_pkey PRIMARY KEY (id)
);


-- oto_cargo.urls definition

-- Drop table

-- DROP TABLE oto_cargo.urls;

CREATE TABLE oto_cargo.urls (
id uuid NOT NULL,
url text NOT NULL,
pattern text NOT NULL,
"method" text NOT NULL,
CONSTRAINT oto_urls_pattern_uk UNIQUE (url, pattern, method),
CONSTRAINT oto_urls_url_uk UNIQUE (url, method),
CONSTRAINT users_urls_pkey PRIMARY KEY (id)
);




-- oto_cargo.waypoint definition

-- Drop table

-- DROP TABLE oto_cargo.waypoint;

CREATE TABLE oto_cargo.waypoint (
    id uuid NOT NULL,
    request_id uuid NULL,
    address_id uuid NULL,
    ordering_index int4 NULL,
    wait_time int8 NULL,
    checkin_automatic bool NULL DEFAULT false, -- Статус автоматического чекина
    checkin_manual bool NULL DEFAULT false, -- Статус ручного чекина
    organization varchar(2000) NULL, -- Организация
    CONSTRAINT waypoint_pkey PRIMARY KEY (id),
    CONSTRAINT waypoint_uk UNIQUE (request_id, address_id, ordering_index)
);
CREATE INDEX oto_waypoint_request_id_idx ON oto_cargo.waypoint USING btree (request_id);

-- Column comments

COMMENT ON COLUMN oto_cargo.waypoint.checkin_automatic IS 'Статус автоматического чекина';
COMMENT ON COLUMN oto_cargo.waypoint.checkin_manual IS 'Статус ручного чекина';
COMMENT ON COLUMN oto_cargo.waypoint.organization IS 'Организация';


-- oto_cargo.organization definition

-- Drop table

-- DROP TABLE oto_cargo.organization;

CREATE TABLE oto_cargo.organization (
    id uuid NOT NULL, -- Идентификатор
    official_name varchar(255) NULL, -- Название
    address varchar NULL, -- Адрес
    organization_group_id uuid NULL, -- Идентификатор группы организации
    CONSTRAINT organization_pkey PRIMARY KEY (id),
    CONSTRAINT ogranization_group_organization_fk FOREIGN KEY (organization_group_id) REFERENCES oto_cargo.organization_group(id)
);
COMMENT ON TABLE oto_cargo.organization IS 'Организации';

-- Column comments

COMMENT ON COLUMN oto_cargo.organization.id IS 'Идентификатор';
COMMENT ON COLUMN oto_cargo.organization.official_name IS 'Название';
COMMENT ON COLUMN oto_cargo.organization.address IS 'Адрес';
COMMENT ON COLUMN oto_cargo.organization.organization_group_id IS 'Идентификатор группы организации';


-- oto_cargo.request definition

-- Drop table

-- DROP TABLE oto_cargo.request;

CREATE TABLE oto_cargo.request (
id uuid NOT NULL,
humanreadableid varchar(100) NULL,
approved_by_id uuid NULL,
author_id uuid NULL,
passenger_id uuid NULL,
creation_time timestamp NULL,
desired_date timestamp NULL,
finished_time timestamp NULL,
"transport_type" varchar(255) NULL,
approval_state varchar(255) NULL,
approval_date timestamp NULL,
"request_status" varchar(255) NULL,
tariff_id uuid NULL,
actual_cost float8 NULL,
actual_distance float8 NULL,
total_waiting_time int8 NULL,
comment_for_driver text NULL,
rating_mark int4 NULL,
rating_comment varchar(255) NULL,
rating_advantages jsonb NULL,
rating_drawbacks jsonb NULL,
start_waypoint_id uuid NULL,
end_waypoint_id uuid NULL,
expected_cost float8 NULL,
expected_distance float8 NULL,
expected_time int8 NULL,
contractor_id uuid NULL, -- Каршеринговая компания
request_status_code int4 NULL, -- Подстатус заявки
dispatcher_id uuid NULL, -- Идентификатор сотрудника диспетчера
payment_type_code_main int4 NULL, -- Код вида основной оплаты
payment_price_main int8 NULL, -- Сумма основной оплаты, коп
payment_type_code_optional int4 NULL, -- Код вида дополнительной оплаты
payment_price_optional int8 NULL, -- Сумма дополнительной оплаты, коп
deadline_state int4 NULL, -- Индикатор контрольного срока 0 - NONE, 1 - YELLOW, 2 - RED
deadline timestamp NULL, -- Дата и время наступления контрольного срока
sender_id uuid NULL, -- Отправитель заявки на грузоперевозку
recipient_id uuid NULL, -- Отправитель заявки на грузоперевозку
transfer_time timestamp NULL, -- Дата завершения сбора груза
shipment_time timestamp NULL, -- Дата завершения доставки груза
ride_id uuid NULL,
sender_phone varchar(40) NULL, -- Телефон отправителя
recipient_phone varchar(40) NULL, -- Телефон получателя
sender_organization varchar(128) NULL, -- Организация отправитель
recipient_organization varchar(128) NULL, -- Организация получатель
volume float8 NULL, -- Общий объем груза
weight float8 NULL, -- Общий вес груза
"source" varchar(100) NULL DEFAULT 'UNDEFINED'::character varying, -- Источник создания заявки
trip_id uuid NULL, -- ID маршрута
cargo_types text NULL, -- Перечисление типов груза в заявке
driver jsonb NULL, -- Отпечаток водителя
vehicle jsonb NULL, -- Отпечаток автомобиля
recipient_name text NULL, -- имя получателя
sender_name text NULL, -- имя отправителя
author_name text NULL, -- Имя автора доставки
author_phone text NULL, -- Телефон автора доставки
author_organization text NULL, -- Организация автора доставки
trip_humanreadableid text NULL, -- Человекочитаемый ID маршрута
organization_id uuid NULL, -- ID организации от которой сделана запрос на доставку
loaders int4 NOT NULL DEFAULT 0, -- количество грузчиков
control_date timestamp NULL, -- Контрольная дата доставки, sla с учетом выходных дней
is_template bool NULL, -- Запрос созданный по расписанию (регулярная перевозка)
add_contact_phone varchar NULL,
add_contact_fio varchar NULL,
executor_group_id uuid NULL,
executor_group_name varchar NULL,
time_zone text NULL, -- Таймзона
request_type varchar(100) NULL,
fraud_message text NULL, -- Сообщение о фроде
CONSTRAINT oto_request_humanreadableid_transporttype_uk UNIQUE (humanreadableid, transport_type),
CONSTRAINT request_pkey PRIMARY KEY (id),
CONSTRAINT request_employee_recipient_id_id_fk FOREIGN KEY (recipient_id) REFERENCES oto_cargo.employee(id) ON DELETE CASCADE,
CONSTRAINT request_employee_sender_id_id_fk FOREIGN KEY (sender_id) REFERENCES oto_cargo.employee(id) ON DELETE CASCADE
);
CREATE INDEX oto_request_contractor_id_idx ON oto_cargo.request USING btree (contractor_id);
CREATE INDEX oto_request_end_waypoint_id_idx ON oto_cargo.request USING btree (end_waypoint_id);
CREATE INDEX oto_request_organization_id_idx ON oto_cargo.request USING btree (organization_id);
CREATE INDEX oto_request_passenger_id_idx ON oto_cargo.request USING btree (passenger_id);
CREATE INDEX oto_request_recipient_id_idx ON oto_cargo.request USING btree (recipient_id);
CREATE INDEX oto_request_sender_id_idx ON oto_cargo.request USING btree (sender_id);
CREATE INDEX oto_request_start_waypoint_id_idx ON oto_cargo.request USING btree (start_waypoint_id);
CREATE INDEX oto_request_test_idx ON oto_cargo.request USING btree (transport_type);
CREATE INDEX oto_request_trip_id_idx ON oto_cargo.request USING btree (trip_id);
CREATE INDEX request_contractor_id_idx ON oto_cargo.request USING btree (contractor_id);
CREATE INDEX request_executor_group_id_idx ON oto_cargo.request USING btree (executor_group_id);
CREATE INDEX request_executor_group_name_idx ON oto_cargo.request USING btree (executor_group_name);
CREATE INDEX request_organization_id_idx ON oto_cargo.request USING btree (organization_id);
CREATE INDEX request_passenger_id_idx_manual ON oto_cargo.request USING btree (passenger_id);
CREATE INDEX request_request_type_idx ON oto_cargo.request USING btree (request_type);
CREATE INDEX request_ride_id_idx ON oto_cargo.request USING btree (ride_id);
CREATE INDEX request_trip_id_idx ON oto_cargo.request USING btree (trip_id);

-- Column comments

COMMENT ON COLUMN oto_cargo.request.contractor_id IS 'Каршеринговая компания';
COMMENT ON COLUMN oto_cargo.request.request_status_code IS 'Подстатус заявки';
COMMENT ON COLUMN oto_cargo.request.dispatcher_id IS 'Идентификатор сотрудника диспетчера';
COMMENT ON COLUMN oto_cargo.request.payment_type_code_main IS 'Код вида основной оплаты';
COMMENT ON COLUMN oto_cargo.request.payment_price_main IS 'Сумма основной оплаты, коп';
COMMENT ON COLUMN oto_cargo.request.payment_type_code_optional IS 'Код вида дополнительной оплаты';
COMMENT ON COLUMN oto_cargo.request.payment_price_optional IS 'Сумма дополнительной оплаты, коп';
COMMENT ON COLUMN oto_cargo.request.deadline_state IS 'Индикатор контрольного срока 0 - NONE, 1 - YELLOW, 2 - RED';
COMMENT ON COLUMN oto_cargo.request.deadline IS 'Дата и время наступления контрольного срока';
COMMENT ON COLUMN oto_cargo.request.sender_id IS 'Отправитель заявки на грузоперевозку';
COMMENT ON COLUMN oto_cargo.request.recipient_id IS 'Отправитель заявки на грузоперевозку';
COMMENT ON COLUMN oto_cargo.request.transfer_time IS 'Дата завершения сбора груза';
COMMENT ON COLUMN oto_cargo.request.shipment_time IS 'Дата завершения доставки груза';
COMMENT ON COLUMN oto_cargo.request.sender_phone IS 'Телефон отправителя';
COMMENT ON COLUMN oto_cargo.request.recipient_phone IS 'Телефон получателя';
COMMENT ON COLUMN oto_cargo.request.sender_organization IS 'Организация отправитель';
COMMENT ON COLUMN oto_cargo.request.recipient_organization IS 'Организация получатель';
COMMENT ON COLUMN oto_cargo.request.volume IS 'Общий объем груза';
COMMENT ON COLUMN oto_cargo.request.weight IS 'Общий вес груза';
COMMENT ON COLUMN oto_cargo.request."source" IS 'Источник создания заявки';
COMMENT ON COLUMN oto_cargo.request.trip_id IS 'ID маршрута';
COMMENT ON COLUMN oto_cargo.request.cargo_types IS 'Перечисление типов груза в заявке';
COMMENT ON COLUMN oto_cargo.request.driver IS 'Отпечаток водителя';
COMMENT ON COLUMN oto_cargo.request.vehicle IS 'Отпечаток автомобиля';
COMMENT ON COLUMN oto_cargo.request.recipient_name IS 'имя получателя';
COMMENT ON COLUMN oto_cargo.request.sender_name IS 'имя отправителя';
COMMENT ON COLUMN oto_cargo.request.author_name IS 'Имя автора доставки';
COMMENT ON COLUMN oto_cargo.request.author_phone IS 'Телефон автора доставки';
COMMENT ON COLUMN oto_cargo.request.author_organization IS 'Организация автора доставки';
COMMENT ON COLUMN oto_cargo.request.trip_humanreadableid IS 'Человекочитаемый ID маршрута';
COMMENT ON COLUMN oto_cargo.request.organization_id IS 'ID организации от которой сделана запрос на доставку';
COMMENT ON COLUMN oto_cargo.request.loaders IS 'количество грузчиков';
COMMENT ON COLUMN oto_cargo.request.control_date IS 'Контрольная дата доставки, sla с учетом выходных дней';
COMMENT ON COLUMN oto_cargo.request.is_template IS 'Запрос созданный по расписанию (регулярная перевозка)';
COMMENT ON COLUMN oto_cargo.request.time_zone IS 'Таймзона';
COMMENT ON COLUMN oto_cargo.request.fraud_message IS 'Сообщение о фроде';


-- oto_cargo.roles definition

-- Drop table

-- DROP TABLE oto_cargo.roles;

CREATE TABLE oto_cargo.roles (
    "role" text NOT NULL,
    url_id uuid NOT NULL,
    CONSTRAINT user_roles_ukey UNIQUE (url_id, role),
    CONSTRAINT role_urls_fkey FOREIGN KEY (url_id) REFERENCES oto_cargo.urls(id)
);




-- oto_cargo.waypoint_contact definition

-- Drop table

-- DROP TABLE oto_cargo.waypoint_contact;

CREATE TABLE oto_cargo.waypoint_contact (
    id uuid NOT NULL, -- Идентификатор контакта
    waypoint_id uuid NOT NULL, -- Идентификатор связанной точки
    mobile_phone varchar(255) NULL, -- Мобильный телефон
    fullname varchar(255) NULL, -- ФИО
    employee_id uuid NULL, -- Идентификатор сотрудника
    CONSTRAINT waypoint_contact_pkey PRIMARY KEY (id),
    CONSTRAINT fk_waypoint_contact_employee FOREIGN KEY (employee_id) REFERENCES oto_cargo.employee(id),
    CONSTRAINT fk_waypoint_contact_waypoint FOREIGN KEY (waypoint_id) REFERENCES oto_cargo.waypoint(id)
);

-- Column comments

COMMENT ON COLUMN oto_cargo.waypoint_contact.id IS 'Идентификатор контакта';
COMMENT ON COLUMN oto_cargo.waypoint_contact.waypoint_id IS 'Идентификатор связанной точки';
COMMENT ON COLUMN oto_cargo.waypoint_contact.mobile_phone IS 'Мобильный телефон';
COMMENT ON COLUMN oto_cargo.waypoint_contact.fullname IS 'ФИО';
COMMENT ON COLUMN oto_cargo.waypoint_contact.employee_id IS 'Идентификатор сотрудника';


-- oto_cargo.request_status_overdue_message definition

-- Drop table

-- DROP TABLE oto_cargo.request_status_overdue_message;

CREATE TABLE oto_cargo.request_status_overdue_message (
    id uuid NULL, -- ID
    request_id uuid NULL, -- ID заявки
    trip_request_status varchar NULL, -- Статус заявки (на поездку)
    carsharing_join_request_status varchar NULL, -- Статус заявки (на подключение к копр. каршерингу)
    deadline_chrono_unit varchar NULL, -- Единица измерения времени КС
    deadline_value int2 NULL, -- Значение КС
    overdue_time timestamp NULL -- Время, в которое был превышен КС
);
COMMENT ON TABLE oto_cargo.request_status_overdue_message IS 'Просроченные по КС заявки';

-- Column comments

COMMENT ON COLUMN oto_cargo.request_status_overdue_message.id IS 'ID';
COMMENT ON COLUMN oto_cargo.request_status_overdue_message.request_id IS 'ID заявки';
COMMENT ON COLUMN oto_cargo.request_status_overdue_message.trip_request_status IS 'Статус заявки (на поездку)';
COMMENT ON COLUMN oto_cargo.request_status_overdue_message.carsharing_join_request_status IS 'Статус заявки (на подключение к копр. каршерингу)';
COMMENT ON COLUMN oto_cargo.request_status_overdue_message.deadline_chrono_unit IS 'Единица измерения времени КС';
COMMENT ON COLUMN oto_cargo.request_status_overdue_message.deadline_value IS 'Значение КС';
COMMENT ON COLUMN oto_cargo.request_status_overdue_message.overdue_time IS 'Время, в которое был превышен КС';


CREATE OR REPLACE FUNCTION oto_cargo.calculate_date_by_percent(desired_date timestamp without time zone, control_date timestamp without time zone, percent numeric)
RETURNS date
LANGUAGE plpgsql
IMMUTABLE STRICT
AS $function$
BEGIN
    RETURN (desired_date::DATE + ((control_date::DATE - desired_date::DATE) * (100-percent)/100)::INTEGER);
END;
$function$
;
