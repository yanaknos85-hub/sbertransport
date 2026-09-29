-- =============================================
-- Тестовые данные для схемы exchange_request
-- Для интеграционных тестов и разработки
-- =============================================

-- 1. Очистка (опционально, раскомментируйте при необходимости)
/*
DELETE FROM exchange_request.attachments;
DELETE FROM exchange_request.special_condition;
DELETE FROM exchange_request.vehicle_requirements;
DELETE FROM exchange_request.cargo_details;
DELETE FROM exchange_request.waypoint;
DELETE FROM exchange_request.request;
DELETE FROM exchange_request.users;
DELETE FROM exchange_request.organization;
*/

-- 4. Вставка: Заявка
INSERT INTO exchange_request.request (
    id,
    humanreadable_id,
    internal_id,
    owner_id,
    status,
    use_etrn,
    view_type,
    payment_form,
    payment_terms,
    payment_days,
    request_created,
    sender_fio,
    sender_phone,
    recipient_fio,
    recipient_phone,
    created_at,
    published_at,
    expires_at,
    cost_request,
    vat_include,
    organization_id
) VALUES (
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380001',
    'ОР-202602-0000001',
    'INT-REQ-001',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380005',
    'CARRIER_SELECTED',
    TRUE,
    'FIXED',
    'NON_CASH',
    'PREPAYMENT',
    NULL,
    CURRENT_DATE,
    'Алексей Кузнецов',
    '+7 916 123-45-67',
    'Марина Петрова',
    '8-903-987-65-43',
    NOW(),
    NOW(),
    NOW() + INTERVAL '60 days',
    750000.00,
    TRUE,
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004'
);

-- 5. Вставка: Точки маршрута (погрузка и выгрузка)
-- Погрузка
INSERT INTO exchange_request.waypoint (
    id,
    ordering_index,
    radius,
    request_id,
    type,
    address_info,
    date,
    from_time,
    to_time
) VALUES (
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380002',
    0,
    500,
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380001',
    'LOAD',
    '{
      "city": "Москва",
      "street": "ул. Ленина, 10",
      "latitude": 55.7558,
      "longitude": 37.6173
    }'::JSONB,
    (NOW() + INTERVAL '1 day')::date,
    (NOW() + INTERVAL '1 day')::time,
    (NOW() + INTERVAL '2 days')::time
);

-- Выгрузка
INSERT INTO exchange_request.waypoint (
    id,
    ordering_index,
    radius,
    request_id,
    type,
    address_info,
    date,
    from_time,
    to_time
) VALUES (
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380003',
    1,
    300,
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380001',
    'UNLOAD',
    '{
      "city": "Санкт-Петербург",
      "street": "Невский проспект, 50",
      "latitude": 59.9388,
      "longitude": 30.3165
    }'::JSONB,
    (NOW() + INTERVAL '3 days')::date,
    (NOW() + INTERVAL '3 days')::time,
    (NOW() + INTERVAL '4 days')::time
);

-- 6. Вставка: Детали груза
INSERT INTO exchange_request.cargo_details (
    id,
    request_id,
    weight_kg,
    volume_m3,
    declared_value,
    length,
    width,
    height,
    cargo_type,
    cargo_package
) VALUES (
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380001',
    5000.00,
    25.500,
    1500000.00,
    10.00,
    2.40,
    2.20,
    '["electronics"]'::JSONB,
    '["pallet"]'::JSONB
);

-- 7. Вставка: Требования к транспорту
INSERT INTO exchange_request.vehicle_requirements (
    id,
    request_id,
    load_type,
    unload_type,
    capacity_m3,
    load_capacity,
    no_additional_load,
    vehicle_body_type,
    vehicle_extra_features,
    comment
) VALUES (
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380005',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380001',
    'rear',
    'rear',
    30.000,
    6.000,
    TRUE,
    '["tent"]'::JSONB,
    '["tail_lift"]'::JSONB,
    'Прошу прислать машину с чистым кузовом'
);

-- 8. Вставка: Особые условия
INSERT INTO exchange_request.special_conditions(
    id,
    request_id,
    is_dangerous,
    dangerous_class,
    has_temperature,
    temp_min,
    temp_max,
    is_oversized,
    other_conditions
) VALUES (
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380006',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380001',
    FALSE,
    NULL,
    TRUE,
    2,
    8,
    FALSE,
    'Хранить в прохладном месте, избегать прямых солнечных лучей'
);

-- 9. Вставка: Вложения
-- Фото груза
INSERT INTO exchange_request.attachments (
    id,
    request_id,
    file_type,
    original_name,
    storage_path,
    file_size,
    mime_type,
    upload_at
) VALUES (
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380007',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380001',
    'CARGO_PHOTO',
    'photo1.jpg',
    '/uploads/2026/02/photo1.jpg',
    1024500,
    'image/jpeg',
    NOW()
);

-- Документ
INSERT INTO exchange_request.attachments (
    id,
    request_id,
    file_type,
    original_name,
    storage_path,
    file_size,
    mime_type,
    upload_at
) VALUES (
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380008',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380001',
    'DOCUMENT',
    'invoice.pdf',
    '/uploads/2026/02/invoice.pdf',
    204800,
    'application/pdf',
    NOW()
);

-- 10. Вставка: CarrierReply (отклики перевозчиков)
INSERT INTO exchange_request.request_carrier_reply (
    id,
    request_id,
    reply,
    organization_id,
    created_at
) VALUES (
    gen_random_uuid(),
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380001',
    '{
      "auto": {
        "name": "Грузовик",
        "mark": "Volvo",
        "model": "FH16",
        "year": "2020",
        "capacity": "15 тонн",
        "regNumber": "А123ВС777"
      },
      "driver": {
        "fio": "Иван Иванов",
        "phone": "+7 900 123-45-67"
      },
      "cost": 50000.00,
      "comment": "Готовы перевезти ваш груз"
    }'::JSONB,
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004',
    NOW()
);

INSERT INTO exchange_request.request_carrier_reply (
    id,
    request_id,
    reply,
    organization_id,
    created_at
) VALUES (
    gen_random_uuid(),
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380001',
    '{
      "auto": {
        "name": "Грузовик",
        "mark": "KAMAZ",
        "model": "6520",
        "year": "2019",
        "capacity": "20 тонн",
        "regNumber": "К567МР777"
      },
      "driver": {
        "fio": "Петр Петров",
        "phone": "+7 900 987-65-43"
      },
      "cost": 45000.00,
      "comment": "Самая надежная перевозка"
    }'::JSONB,
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380088',
    NOW()
);