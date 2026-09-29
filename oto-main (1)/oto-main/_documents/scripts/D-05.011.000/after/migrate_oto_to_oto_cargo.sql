TRUNCATE TABLE
oto_cargo.address,
oto_cargo.contract,
oto_cargo.contractor,
oto_cargo.department,
oto_cargo.employee,
oto_cargo.evaluation,
oto_cargo.evaluation_reasons,
oto_cargo.organization,
oto_cargo.organization_group,
oto_cargo."position",
oto_cargo.request,
oto_cargo.request_status_overdue_message,
oto_cargo.roles,
oto_cargo.routelist,
oto_cargo."tariff",
oto_cargo.template_for_cargo,
oto_cargo.urls,
oto_cargo.waypoint,
oto_cargo.waypoint_contact
RESTART IDENTITY CASCADE;

-- ============================================================================
-- Миграция данных из схемы oto в oto_cargo
-- Переносятся только грузовые заявки: transport_type IN ('COURIER','DEDICATED','DOMESTIC_COURIER','INTERREGIONAL','INDIVIDUAL')
-- И связанные с ними сущности. Справочники переносятся целиком.
-- ============================================================================
-- ВНИМАНИЕ: скрипт идемпотентен — использует INSERT ... ON CONFLICT DO NOTHING
-- ============================================================================


-- ============================================================================
-- 1. Справочные таблицы (без фильтрации — переносим всё)
-- ============================================================================

-- 1.1 address — только те, на которые есть ссылка в waypoint грузовых заявок
INSERT INTO oto_cargo.address
(id, building, city, country, house, region, street, "structure", exist_in_vsp_tb_registry, address_string)
SELECT
    a.id, a.building, a.city, a.country, a.house, a.region, a.street, a."structure",
    a.exist_in_vsp_tb_registry, a.address_string
FROM oto.address a
     JOIN oto.waypoint w ON w.address_id = a.id
         JOIN oto.request r ON r.id = w.request_id
WHERE r.transport_type IN ('COURIER','DEDICATED','DOMESTIC_COURIER','INTERREGIONAL','INDIVIDUAL')
ON CONFLICT (id) DO NOTHING;

-- 1.2 organization_group
INSERT INTO oto_cargo.organization_group
(id, "name", internal)
SELECT
    og.id, og."name", og.internal
FROM oto.organization_group og
ON CONFLICT (id) DO NOTHING;

-- 1.3 organization
INSERT INTO oto_cargo.organization
(id, official_name, address, organization_group_id)
SELECT
    o.id, o.official_name, o.address, o.organization_group_id
FROM oto.organization o
ON CONFLICT (id) DO NOTHING;

-- 1.4 department
INSERT INTO oto_cargo.department
(id, department_name, parent_id, organization_id, human_readable_id)
SELECT
    d.id, d.department_name, d.parent_id, d.organization_id, d.human_readable_id
FROM oto.department d
ON CONFLICT (id) DO NOTHING;

-- 1.5 position
INSERT INTO oto_cargo."position"
(id, position_name)
SELECT
    p.id, p.position_name
FROM oto."position" p
ON CONFLICT (id) DO NOTHING;

-- 1.6 employee
INSERT INTO oto_cargo.employee
(id, first_name, last_name, patronymic, personnel_number, human_readable_id,
    mobile_phone, position_id, department_id, organization_id, itinerant_type,
    cost_center, marriage_certificate_number, user_id)
SELECT
    e.id, e.first_name, e.last_name, e.patronymic, e.personnel_number, e.human_readable_id,
    e.mobile_phone, e.position_id, e.department_id, e.organization_id, e.itinerant_type,
    e.cost_center, e.marriage_certificate_number, e.user_id
FROM oto.employee e
ON CONFLICT (id) DO UPDATE SET
    first_name = EXCLUDED.first_name,
    last_name = EXCLUDED.last_name,
    patronymic = EXCLUDED.patronymic,
    personnel_number = EXCLUDED.personnel_number,
    human_readable_id = EXCLUDED.human_readable_id,
    mobile_phone = EXCLUDED.mobile_phone,
    position_id = EXCLUDED.position_id,
    department_id = EXCLUDED.department_id,
    organization_id = EXCLUDED.organization_id,
    itinerant_type = EXCLUDED.itinerant_type,
    cost_center = EXCLUDED.cost_center,
    marriage_certificate_number = EXCLUDED.marriage_certificate_number,
    user_id = EXCLUDED.user_id;

-- 1.7 contract
INSERT INTO oto_cargo.contract
(id, contractor_id, active)
SELECT
    c.id, c.contractor_id, c.active
FROM oto.contract c
ON CONFLICT (id) DO NOTHING;

-- 1.8 contractor
INSERT INTO oto_cargo.contractor
(id, "name", active)
SELECT
    c.id, c."name", c.active
FROM oto.contractor c
ON CONFLICT (id) DO NOTHING;

-- 1.9 tariff
INSERT INTO oto_cargo.tariff
(id, organization_id, transport_type, humanreadableid, service_type, region, active,
    car_service_cost, ride_cost_per_km, ride_cost_per_min, taxi_class,
    wait_cost_per_min, wait_cost_per_min_intermediate,
    min_ride_distance_cost, min_ride_time_cost,
    contractor_max_diff_computed_distance_percent, contractor_max_diff_fact_distance_percent,
    contractor_max_diff_computed_cost_percent, contractor_max_diff_contractor_cost_percent,
    contractor_max_diff_computed_waiting_percent, contract_id,
    coef_traffic, coef_child_seat, coef_pet_transport, coef_casco,
    coef_work_day_morning, coef_work_day_noon, coef_work_day_evening, coef_work_day_night, coef_day_off)
SELECT
    t.id, t.organization_id, t.transport_type, t.humanreadableid, t.service_type, t.region, t.active,
    t.car_service_cost, t.ride_cost_per_km, t.ride_cost_per_min, t.taxi_class,
    t.wait_cost_per_min, t.wait_cost_per_min_intermediate,
    t.min_ride_distance_cost, t.min_ride_time_cost,
    t.contractor_max_diff_computed_distance_percent, t.contractor_max_diff_fact_distance_percent,
    t.contractor_max_diff_computed_cost_percent, t.contractor_max_diff_contractor_cost_percent,
    t.contractor_max_diff_computed_waiting_percent, t.contract_id,
    t.coef_traffic, t.coef_child_seat, t.coef_pet_transport, t.coef_casco,
    t.coef_work_day_morning, t.coef_work_day_noon, t.coef_work_day_evening, t.coef_work_day_night, t.coef_day_off
FROM oto.tariff t
ON CONFLICT (id) DO NOTHING;

-- 1.10 routelist
INSERT INTO oto_cargo.routelist
(id, humanreadableid, tariff_id, contractor_info, "status", status_code, "cost", distance, active)
SELECT
    rl.id, rl.humanreadableid, rl.tariff_id, rl.contractor_info, rl."status", rl.status_code, rl."cost", rl.distance, rl.active
FROM oto.routelist rl
ON CONFLICT (id) DO NOTHING;


-- ============================================================================
-- 2. template_for_cargo — все записи (специфичны для груза)
-- ============================================================================
INSERT INTO oto_cargo.template_for_cargo
(id, humanreadableid, transport_type, creation_time, cron_expression, "status",
    requests_date_delivery, "template", total_cost, organization_id,
    recipient_name, sender_name, recipient_address, sender_address, author_id)
SELECT
    tfc.id, tfc.humanreadableid, tfc.transport_type, tfc.creation_time, tfc.cron_expression, tfc."status",
    tfc.requests_date_delivery, tfc."template", tfc.total_cost, tfc.organization_id,
    tfc.recipient_name, tfc.sender_name, tfc.recipient_address, tfc.sender_address, tfc.author_id
FROM oto.template_for_cargo tfc
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- 3. request — только грузовые заявки
-- Из старой схемы удалены поля: carsharing_class, public_compensation_document_exist,
--    shared_ride_id, purpose_id, carrier, savings, personal_car, employee_driver_id,
--    coop_trip, passenger_count, actual_duration, additional_options, group_transfer_class, limit_id
-- ============================================================================
INSERT INTO oto_cargo.request
(id, humanreadableid, approved_by_id, author_id, passenger_id,
creation_time, desired_date, finished_time, transport_type,
approval_state, approval_date, request_status, tariff_id,
actual_cost, actual_distance,
total_waiting_time, comment_for_driver,
rating_mark, rating_comment, rating_advantages, rating_drawbacks,
start_waypoint_id, end_waypoint_id,
expected_cost, expected_distance, expected_time,
contractor_id, request_status_code, dispatcher_id,
payment_type_code_main, payment_price_main, payment_type_code_optional, payment_price_optional,
deadline_state, deadline,
sender_id, recipient_id, transfer_time, shipment_time, ride_id,
sender_phone, recipient_phone, sender_organization, recipient_organization,
volume, weight, source, trip_id, cargo_types,
driver, vehicle, recipient_name, sender_name, author_name, author_phone, author_organization,
trip_humanreadableid, organization_id, loaders, control_date,
is_template, add_contact_phone, add_contact_fio,
executor_group_id, executor_group_name, time_zone, request_type, fraud_message)
SELECT
r.id, r.humanreadableid, r.approved_by_id, r.author_id, r.passenger_id,
r.creation_time, r.desired_date, r.finished_time, r.transport_type,
r.approval_state, r.approval_date, r.request_status, r.tariff_id,
r.actual_cost, r.actual_distance,
r.total_waiting_time, r.comment_for_driver,
r.rating_mark, r.rating_comment, r.rating_advantages, r.rating_drawbacks,
r.start_waypoint_id, r.end_waypoint_id,
r.expected_cost, r.expected_distance, r.expected_time,
r.contractor_id, r.request_status_code, r.dispatcher_id,
r.payment_type_code_main, r.payment_price_main, r.payment_type_code_optional, r.payment_price_optional,
r.deadline_state, r.deadline,
r.sender_id, r.recipient_id, r.transfer_time, r.shipment_time, r.ride_id,
r.sender_phone, r.recipient_phone, r.sender_organization, r.recipient_organization,
r.volume, r.weight, r.source, r.trip_id, r.cargo_types,
r.driver, r.vehicle, r.recipient_name, r.sender_name, r.author_name, r.author_phone, r.author_organization,
r.trip_humanreadableid, r.organization_id, r.loaders, r.control_date,
r.is_template, r.add_contact_phone, r.add_contact_fio,
r.executor_group_id, r.executor_group_name, r.time_zone, r.request_type, r.fraud_message
FROM oto.request r
WHERE r.transport_type IN ('COURIER','DEDICATED','DOMESTIC_COURIER','INTERREGIONAL','INDIVIDUAL')
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- 4. waypoint — только для грузовых заявок
-- ============================================================================
INSERT INTO oto_cargo.waypoint
(id, request_id, address_id, ordering_index, wait_time,
    checkin_automatic, checkin_manual, organization)
SELECT
    w.id, w.request_id, w.address_id, w.ordering_index, w.wait_time,
    w.checkin_automatic, w.checkin_manual, w.organization
FROM oto.waypoint w
     JOIN oto.request r ON r.id = w.request_id
WHERE r.transport_type IN ('COURIER','DEDICATED','DOMESTIC_COURIER','INTERREGIONAL','INDIVIDUAL')
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- 5. waypoint_contact — для waypoint грузовых заявок
-- ============================================================================
INSERT INTO oto_cargo.waypoint_contact
(id, waypoint_id, mobile_phone, fullname, employee_id)
SELECT
    wc.id, wc.waypoint_id, wc.mobile_phone, wc.fullname, wc.employee_id
FROM oto.waypoint_contact wc
     JOIN oto.waypoint w ON w.id = wc.waypoint_id
         JOIN oto.request r ON r.id = w.request_id
WHERE r.transport_type IN ('COURIER','DEDICATED','DOMESTIC_COURIER','INTERREGIONAL','INDIVIDUAL')
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- 6. evaluation — только для грузовых заявок
-- ============================================================================
INSERT INTO oto_cargo.evaluation
(id, request_id, rating, "comment")
SELECT
    e.id, e.request_id, e.rating, e."comment"
FROM oto.evaluation e
     JOIN oto.request r ON r.id = e.request_id
WHERE r.transport_type IN ('COURIER','DEDICATED','DOMESTIC_COURIER','INTERREGIONAL','INDIVIDUAL')
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- 7. evaluation_reasons — только для evaluation грузовых заявок
-- ============================================================================
INSERT INTO oto_cargo.evaluation_reasons
(evaluation_id, reason)
SELECT
    er.evaluation_id, er.reason
FROM oto.evaluation_reasons er
     JOIN oto.evaluation e ON e.id = er.evaluation_id
         JOIN oto.request r ON r.id = e.request_id
WHERE r.transport_type IN ('COURIER','DEDICATED','DOMESTIC_COURIER','INTERREGIONAL','INDIVIDUAL')
ON CONFLICT (evaluation_id, reason) DO NOTHING;


-- ============================================================================
-- 8. request_status_overdue_message — только для грузовых заявок
-- ============================================================================
INSERT INTO oto_cargo.request_status_overdue_message
(id, request_id, trip_request_status, carsharing_join_request_status,
    deadline_chrono_unit, deadline_value, overdue_time)
SELECT
    rsom.id, rsom.request_id, rsom.trip_request_status, rsom.carsharing_join_request_status,
    rsom.deadline_chrono_unit, rsom.deadline_value, rsom.overdue_time
FROM oto.request_status_overdue_message rsom
     JOIN oto.request r ON r.id = rsom.request_id
WHERE r.transport_type IN ('COURIER','DEDICATED','DOMESTIC_COURIER','INTERREGIONAL','INDIVIDUAL')
ON CONFLICT DO NOTHING;


-- ============================================================================
-- Скрипт назначения ролей для схемы oto_cargo
-- ============================================================================

-- GET /{requestId}/ - детальная информация по заявке (включая грузовые)
CALL migrations.fill_roles('oto_cargo', 'GET /{requestId}/', 'ROLE_ADMIN_DATA_MASTER', true);
CALL migrations.fill_roles('oto_cargo', 'GET /{requestId}/', 'ROLE_ADMIN_CORP_CLIENT', true);
CALL migrations.fill_roles('oto_cargo', 'GET /{requestId}/', 'ROLE_ENGINEER_CORP_CLIENT', true);

-- POST /cargo/ - список заявок по грузоперевозкам
CALL migrations.fill_roles('oto_cargo', 'POST /cargo/', 'ROLE_ADMIN_DATA_MASTER', true);
CALL migrations.fill_roles('oto_cargo', 'POST /cargo/', 'ROLE_ADMIN_CORP_CLIENT', true);
CALL migrations.fill_roles('oto_cargo', 'POST /cargo/', 'ROLE_ENGINEER_CORP_CLIENT', true);

-- GET /cargo/{organizationId}/template/ - шаблоны на регулярную грузоперевозку
CALL migrations.fill_roles('oto_cargo', 'GET /cargo/{organizationId}/template/', 'ROLE_ADMIN_DATA_MASTER', true);
CALL migrations.fill_roles('oto_cargo', 'GET /cargo/{organizationId}/template/', 'ROLE_ADMIN_CORP_CLIENT', true);
CALL migrations.fill_roles('oto_cargo', 'GET /cargo/{organizationId}/template/', 'ROLE_ENGINEER_CORP_CLIENT', true);