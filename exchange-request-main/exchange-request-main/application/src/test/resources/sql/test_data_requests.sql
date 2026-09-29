-- Вставка заявок
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
    request_created,
    created_at,
    expires_at,
    updated_at,
    published_at,
    completed_at,
    organization_id
) VALUES
    -- Заявка 1: принадлежит OWNER_ID_1, статус PUBLISHED
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380003', 'ОП-202602-0000001', 'INT-REQ-001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380001', 'PUBLISHED', true, 'FIXED', 'NON_CASH', 'PREPAYMENT', '2026-02-01', '2026-02-01T10:00:00', '2026-02-03T10:00:00', NULL, NULL, NULL, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004'),

    -- Заявка 2: принадлежит OWNER_ID_1, статус PUBLISHED
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004', 'ОП-202602-0000002', 'INT-REQ-002', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380001', 'PUBLISHED', false, 'FIXED', 'CASH', 'ON_DELIVERY', '2026-02-02', '2026-02-02T11:00:00', '2026-02-04T11:00:00', '2026-02-02T12:00:00', '2026-02-02T12:00:00', NULL, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004'),

    -- Заявка 3: принадлежит OWNER_ID_2, статус PUBLISHED
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380005', 'ОП-202602-0000003', 'INT-REQ-003', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380002', 'PUBLISHED', true, 'FIXED', 'NON_CASH', 'DEFERRED_PAYMENT', '2026-02-03', '2026-02-03T09:00:00', '2026-02-05T09:00:00', NULL, NULL, NULL, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004');

-- Вставка деталей груза
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
) VALUES
    -- Заявка 1
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380003', 500.0, 2.5, 500000.00, 2.5, 1.2, 1.0, '["ELECTRONICS"]'::JSONB, '["BOX"]'::JSONB),

    -- Заявка 2
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004', 1500.0, 12.0, 1200000.00, 3.0, 2.0, 1.8, '["FURNITURE"]'::JSONB, '["PALLET_WRAPPED"]'::JSONB),

    -- Заявка 3
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380005', 300.0, 8.0, 300000.00, 1.5, 1.2, 1.0, '["CLOTHING"]'::JSONB, '["BAG"]'::JSONB);

-- Вставка требований к транспорту
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
) VALUES
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380003', 'rear', 'rear', 20.0, 5.0, true, '["refrigerator"]'::JSONB, '["palletJack"]'::JSONB, 'Требуется рефрижератор'),
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004', 'side', 'rear', 30.0, 10.0, false, '["tarp"]'::JSONB, '[]'::JSONB, 'Можно догружать'),
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380005', 'rear', 'side', 15.0, 8.0, true, '["container"]'::JSONB, '["heater"]'::JSONB, 'Зимний комплект');

-- Вставка особых условий
INSERT INTO exchange_request.special_conditions (
    id,
    request_id,
    is_dangerous,
    dangerous_class,
    has_temperature,
    temp_min,
    temp_max,
    is_oversized,
    other_conditions
) VALUES
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380003', false, null, true, 2, 8, false, 'Хранить при +2..+8°C'),
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004', true, '3', false, null, null, true, 'Негабарит, требуется сопровождение');

-- Вставка контрольных точек
INSERT INTO exchange_request.waypoint (
    id,
    request_id,
    ordering_index,
    type,
    date,
    from_time,
    to_time,
    radius,
    address_info,
    contact_info
) VALUES
    -- Заявка 1: LOAD
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380003', 0, 'LOAD',
     '2026-02-02'::DATE,
     '08:00:00'::TIME,
     '10:00:00'::TIME,
     500,
     '{"latitude":59.9343,"longitude":30.3351,"country":"Россия","region":"Ленинградская область","city":"Санкт-Петербург","street":"Невский проспект","house":"45","settlement":"Санкт-Петербург","district":"Центральный район","building":"2","livingArea":"Дворцовый округ","place":"ТЦ Пассаж","entrance":"Подъезд 1","floor":"3","flat":"12","processInformation":"Частичное совпадение по базе","structure":"А","addressString":"г. Санкт-Петербург, Невский пр., д. 45, стр. 2, подъезд 1, этаж 3, кв. 12","existInVspGosbTbRegistry":false,"addressType":"ORDINARY","vsp":null,"gosb":null}',
     '{"contactPerson": "Иван Иванов", "contactPhone": "+7 916 123-45-67", "organizationInn": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380044"}'
    ),
    -- Заявка 1: UNLOAD
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380003', 1, 'UNLOAD',
     '2026-02-04'::DATE,
     '09:00:00'::TIME,
     '11:00:00'::TIME,
     300,
     '{"latitude":55.7558,"longitude":37.6173,"country":"Россия","region":"Московская область","city":"Москва","street":"Ленинский проспект","house":"32","settlement":"Москва","building":"5","district":"Гагаринский район","livingArea":"Ломоносово","place":"БЦ Ленинградский","entrance":"Подъезд 2","floor":"4","flat":"405","processInformation":"Адрес распознан с высокой точностью","structure":"Корпус Б","addressString":"г. Москва, Ленинский пр., д. 32, стр. 5, подъезд 2, этаж 4, кв. 405","existInVspGosbTbRegistry":true,"addressType":"VSP","vsp":"Московский Центральный Узел","gosb":"Московский ГОСБ"}',
     '{"contactPerson": "Мария Петрова", "contactPhone": "+7 921 987-65-43", "organizationInn": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380055"}'
    );