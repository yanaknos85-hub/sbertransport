-- Вставка 10 заявок в таблицу exchange_request.request
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
    organization_id,
    carrier_organization_id,
    carrier_info  -- Добавлено новое поле
) VALUES
    -- Заявка 1: PUBLISHED, без перевозчика
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380003', 'ОП-202602-0000001', 'INT-REQ-001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380001', 'PUBLISHED', true, 'FIXED', 'NON_CASH', 'PREPAYMENT', '2026-02-01', '2026-02-01T10:00:00', '2026-02-03T10:00:00', NULL, '2026-02-01T10:00:00', NULL, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004', NULL, NULL),

    -- Заявка 2: CARRIER_SELECTED (отклик отправлен)
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004', 'ОП-202602-0000002', 'INT-REQ-002', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380001', 'CARRIER_SELECTED', false, 'FIXED', 'CASH', 'ON_DELIVERY', '2026-02-02', '2026-02-02T11:00:00', '2026-02-04T11:00:00', '2026-02-02T12:00:00', '2026-02-02T12:00:00', NULL, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004', NULL, NULL),

    -- Заявка 3: CARRIER_SELECTED (выбран перевозчик, но не подтвердил)
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380005', 'ОП-202602-0000003', 'INT-REQ-003', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380002', 'CARRIER_SELECTED', true, 'FIXED', 'NON_CASH', 'DEFERRED_PAYMENT', '2026-02-03', '2026-02-03T09:00:00', '2026-02-05T09:00:00', NULL, '2026-02-03T09:00:00', NULL, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380088',
     '{
       "auto": {
         "model": "MAZ-6440",
         "mark": "МАЗ",
         "regNumber": "М456СР178"
       },
       "driver": {
         "fio": "Григорьев Станислав Михайлович",
         "phone": "+7 921 444-55-66",
         "licence": "1784567890"
       },
       "cost": 700000.00,
       "vat": "VAT_20",
       "comment": "Быстрая погрузка, опыт доставки одежды"
     }'::JSONB
    ),

    -- Заявка 4: CONFIRMED, назначен перевозчик
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380006', 'ОП-202602-0000004', 'INT-REQ-004', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380002', 'CONFIRMED', true, 'FIXED', 'NON_CASH', 'PREPAYMENT', '2026-02-04', '2026-02-04T10:00:00', '2026-02-06T10:00:00', '2026-02-04T11:00:00', '2026-02-04T11:00:00', NULL, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380088',
     '{
       "auto": {
         "model": "DAF XF 105",
         "mark": "DAF",
         "regNumber": "К678АР777"
       },
       "driver": {
         "fio": "Сергеев Николай Алексеевич",
         "phone": "+7 926 111-22-33",
         "licence": "7773456789"
       },
       "cost": 1800000.00,
       "vat": "VAT_20",
       "comment": "Есть опыт перевозки техники"
     }'::JSONB
    ),

    -- Заявка 5: CONFIRMED, другой перевозчик
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380007', 'ОП-202602-0000005', 'INT-REQ-005', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380003', 'CONFIRMED', false, 'FIXED', 'CASH', 'ON_DELIVERY', '2026-02-05', '2026-02-05T12:00:00', '2026-02-07T12:00:00', '2026-02-05T13:00:00', '2026-02-05T13:00:00', NULL, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380089',
     '{
       "auto": {
         "model": "MAN TGX",
         "mark": "MAN",
         "regNumber": "М123КТ888"
       },
       "driver": {
         "fio": "Васильев Дмитрий Сергеевич",
         "phone": "+7 915 444-55-66",
         "licence": "8881234567"
       },
       "cost": 950000.00,
       "vat": "VAT_20",
       "comment": "Готов к отправке завтра"
     }'::JSONB
    ),

    -- Заявка 6: PUBLISHED
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380008', 'ОП-202602-0000006', 'INT-REQ-006', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380001', 'PUBLISHED', true, 'FIXED', 'NON_CASH', 'DEFERRED_PAYMENT', '2026-02-06', '2026-02-06T08:00:00', '2026-02-08T08:00:00', NULL, '2026-02-06T08:00:00', NULL, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004', NULL, NULL),

    -- Заявка 7: PUBLISHED
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380009', 'ОП-202602-0000007', 'INT-REQ-007', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380002', 'PUBLISHED', false, 'FIXED', 'CASH', 'PREPAYMENT', '2026-02-07', '2026-02-07T14:00:00', '2026-02-09T14:00:00', NULL, '2026-02-07T14:00:00', NULL, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004', NULL, NULL),

    -- Заявка 8: CARRIER_SELECTED
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380010', 'ОП-202602-0000008', 'INT-REQ-008', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380003', 'CARRIER_SELECTED', true, 'FIXED', 'NON_CASH', 'ON_DELIVERY', '2026-02-08', '2026-02-08T15:00:00', '2026-02-10T15:00:00', '2026-02-08T16:00:00', '2026-02-08T16:00:00', NULL, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004', NULL, NULL),

    -- Заявка 9: CARRIER_SELECTED
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380011', 'ОП-202602-0000009', 'INT-REQ-009', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380001', 'CARRIER_SELECTED', false, 'FIXED', 'CASH', 'DEFERRED_PAYMENT', '2026-02-09', '2026-02-09T16:00:00', '2026-02-11T16:00:00', '2026-02-09T17:00:00', '2026-02-09T17:00:00', NULL, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004', NULL, NULL),

    -- Заявка 10: PUBLISHED
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380012', 'ОП-202602-0000010', 'INT-REQ-010', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380002', 'PUBLISHED', true, 'FIXED', 'NON_CASH', 'PREPAYMENT', '2026-02-10', '2026-02-10T17:00:00', '2026-02-12T17:00:00', NULL, '2026-02-10T17:00:00', NULL, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004', NULL, NULL);

-- Вставка деталей груза для всех заявок
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
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380005', 300.0, 8.0, 300000.00, 1.5, 1.2, 1.0, '["CLOTHING"]'::JSONB, '["BAG"]'::JSONB),
    -- Заявка 4
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380006', 2000.0, 15.0, 2000000.00, 4.0, 2.5, 2.0, '["MACHINERY"]'::JSONB, '["CRATE"]'::JSONB),
    -- Заявка 5
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380007', 800.0, 6.0, 800000.00, 2.0, 1.8, 1.5, '["GLASS"]'::JSONB, '["PALLET_WRAPPED"]'::JSONB),
    -- Заявка 6
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380008', 1200.0, 10.0, 1200000.00, 3.5, 2.2, 1.7, '["FOOD"]'::JSONB, '["BOX"]'::JSONB),
    -- Заявка 7
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380009', 400.0, 3.0, 400000.00, 1.8, 1.0, 1.0, '["BOOKS"]'::JSONB, '["BAG"]'::JSONB),
    -- Заявка 8
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380010', 3000.0, 25.0, 3000000.00, 5.0, 2.5, 2.4, '["METALS"]'::JSONB, '["UNPACKAGED"]'::JSONB),
    -- Заявка 9
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380011', 600.0, 5.0, 600000.00, 2.2, 1.5, 1.2, '["CHEMICALS"]'::JSONB, '["BARREL"]'::JSONB),
    -- Заявка 10
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380012', 1000.0, 7.5, 1000000.00, 3.0, 2.0, 1.8, '["TEXTILES"]'::JSONB, '["ROLL"]'::JSONB);

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
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380005', 'rear', 'side', 15.0, 8.0, true, '["container"]'::JSONB, '["heater"]'::JSONB, 'Зимний комплект'),
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380006', 'rear', 'rear', 25.0, 12.0, false, '["van"]'::JSONB, '["lift"]'::JSONB, 'Есть подъёмник'),
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380007', 'side', 'side', 18.0, 9.0, true, '["isothermal"]'::JSONB, '[]'::JSONB, 'Требуется чистый кузов'),
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380008', 'rear', 'rear', 22.0, 11.0, false, '["refrigerator"]'::JSONB, '["generator"]'::JSONB, 'Генератор для охлаждения'),
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380009', 'rear', 'rear', 12.0, 6.0, true, '["box"]'::JSONB, '[]'::JSONB, 'Без допогрузки'),
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380010', 'side', 'rear', 40.0, 20.0, false, '["platform"]'::JSONB, '["crane"]'::JSONB, 'Автокран'),
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380011', 'rear', 'rear', 16.0, 7.0, true, '["tanker"]'::JSONB, '["explosionProof"]'::JSONB, 'Взрывозащищённый'),
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380012', 'rear', 'rear', 20.0, 10.0, false, '["tarp"]'::JSONB, '["cover"]'::JSONB, 'Надёжное укрытие');

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
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004', true, '3', false, null, null, true, 'Негабарит, требуется сопровождение'),
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380005', false, null, false, null, null, false, 'Теплоизоляция'),
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380006', false, null, true, -18, -15, false, 'Морозильная камера'),
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380007', true, '8', false, null, null, false, 'Едкие вещества'),
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380008', false, null, false, null, null, false, 'Беречь от влаги'),
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380009', false, null, false, null, null, true, 'Длинномер'),
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380010', false, null, false, null, null, true, 'Сверхтяжёлый груз'),
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380011', true, '3', true, 15, 25, false, 'Легковоспламеняющиеся, температурный режим'),
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380012', false, null, false, null, null, false, 'Ручная погрузка');

-- Вставка контрольных точек (LOAD и UNLOAD для каждой заявки)
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
     '{"latitude":59.9343,"longitude":30.3351,"country":"Россия","region":"Ленинградская область","city":"Санкт-Петербург","street":"Невский проспект","house":"45","settlement":"Санкт-Петербург","district":"Центральный район","building":"2","livingArea":"Дворцовый округ","place":"ТЦ Пассаж","entrance":"Подъезд 1","floor":"3","flat":"12","processInformation":"Частичное совпадение по базе","structure":"А","addressString":"г. Санкт-Петербург, Невский пр., д. 45, стр. 2, подъезд 1, этаж 3, кв. 12","existInVspGosbTbRegistry":false,"addressType":"ORDINARY","vsp":null,"gosb":null}'::jsonb,
     '{"contactPerson": "Иван Иванов", "contactPhone": "+7 916 123-45-67", "organizationInn": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380044"}'::jsonb),

    -- Заявка 1: UNLOAD
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380003', 1, 'UNLOAD',
     '2026-02-04'::DATE,
     '09:00:00'::TIME,
     '11:00:00'::TIME,
     300,
     '{"latitude":55.7558,"longitude":37.6173,"country":"Россия","region":"Московская область","city":"Москва","street":"Ленинский проспект","house":"32","settlement":"Москва","building":"5","district":"Гагаринский район","livingArea":"Ломоносово","place":"БЦ Ленинградский","entrance":"Подъезд 2","floor":"4","flat":"405","processInformation":"Адрес распознан с высокой точностью","structure":"Корпус Б","addressString":"г. Москва, Ленинский пр., д. 32, стр. 5, подъезд 2, этаж 4, кв. 405","existInVspGosbTbRegistry":true,"addressType":"VSP","vsp":"Московский Центральный Узел","gosb":"Московский ГОСБ"}'::jsonb,
     '{"contactPerson": "Мария Петрова", "contactPhone": "+7 921 987-65-43", "organizationInn": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380055"}'::jsonb),

    -- Заявка 2: LOAD
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004', 0, 'LOAD',
     '2026-02-03'::DATE,
     '07:00:00'::TIME,
     '09:00:00'::TIME,
     400,
     '{"latitude":55.7558,"longitude":37.6173,"country":"Россия","region":"Московская область","city":"Москва","street":"Варшавское шоссе","house":"120","settlement":"Москва","building":"1","district":"Южный административный округ","livingArea":"Нагатино-Садовники","place":"ТЦ Мега","entrance":"Шлагбаум 3","floor":"","flat":"","processInformation":"Успешно распознан","structure":"Административное здание","addressString":"г. Москва, Варшавское ш., д. 120, стр. 1, шлагбаум 3","existInVspGosbTbRegistry":false,"addressType":"ORDINARY","vsp":null,"gosb":null}'::jsonb,
     '{"contactPerson": "Алексей Смирнов", "contactPhone": "+7 915 111-22-33", "organizationInn": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380044"}'::jsonb),

    -- Заявка 2: UNLOAD
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004', 1, 'UNLOAD',
     '2026-02-05'::DATE,
     '10:00:00'::TIME,
     '12:00:00'::TIME,
     500,
     '{"latitude":59.9343,"longitude":30.3351,"country":"Россия","region":"Ленинградская область","city":"Санкт-Петербург","street":"Большая Конюшенная","house":"10","settlement":"Санкт-Петербург","building":"3","district":"Центральный район","livingArea":"Мойка","place":"Складской комплекс","entrance":"Секция B","floor":"","flat":"","processInformation":"Адрес найден в справочнике","structure":"Склад B","addressString":"г. Санкт-Петербург, Б. Конюшенная ул., д. 10, стр. 3, секция B","existInVspGosbTbRegistry":true,"addressType":"VSP","vsp":"СПб Центр","gosb":"СПб ГОСБ"}'::jsonb,
     '{"contactPerson": "Елена Кузнецова", "contactPhone": "+7 921 555-66-77", "organizationInn": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380055"}'::jsonb),

    -- Заявка 3: LOAD
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380005', 0, 'LOAD',
     '2026-02-04'::DATE,
     '06:00:00'::TIME,
     '08:00:00'::TIME,
     600,
     '{"latitude":54.7114,"longitude":20.4880,"country":"Россия","region":"Калининградская область","city":"Калининград","street":"Ленина","house":"50","settlement":"Калининград","building":"2","district":"Центральный округ","livingArea":"Центр","place":"Оптовый склад","entrance":"Въезд со двора","floor":"","flat":"","processInformation":"Адрес подтверждён","structure":"Складское помещение","addressString":"г. Калининград, ул. Ленина, д. 50, стр. 2, въезд со двора","existInVspGosbTbRegistry":false,"addressType":"ORDINARY","vsp":null,"gosb":null}'::jsonb,
     '{"contactPerson": "Дмитрий Орлов", "contactPhone": "+7 910 222-33-44", "organizationInn": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380044"}'::jsonb),

    -- Заявка 3: UNLOAD
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380005', 1, 'UNLOAD',
     '2026-02-06'::DATE,
     '11:00:00'::TIME,
     '13:00:00'::TIME,
     400,
     '{"latitude":55.7558,"longitude":37.6173,"country":"Россия","region":"Московская область","city":"Москва","street":"Профсоюзная","house":"90","settlement":"Москва","building":"5","district":"Академический район","livingArea":"Котловка","place":"Торговая база","entrance":"Зона разгрузки 4","floor":"","flat":"","processInformation":"Адрес корректен","structure":"База №4","addressString":"г. Москва, Профсоюзная ул., д. 90, стр. 5, зона разгрузки 4","existInVspGosbTbRegistry":true,"addressType":"VSP","vsp":"Московский Южный","gosb":"Московский ГОСБ"}'::jsonb,
     '{"contactPerson": "Татьяна Волкова", "contactPhone": "+7 926 777-88-99", "organizationInn": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380055"}'::jsonb),

    -- Заявка 4: LOAD
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380006', 0, 'LOAD',
     '2026-02-05'::DATE,
     '05:00:00'::TIME,
     '07:00:00'::TIME,
     700,
     '{"latitude":55.0085,"longitude":82.9362,"country":"Россия","region":"Новосибирская область","city":"Новосибирск","street":"Станционная","house":"20","settlement":"Новосибирск","building":"1","district":"Ленинский район","livingArea":"Станционный","place":"ЖД терминал","entrance":"Платформа 3","floor":"","flat":"","processInformation":"Адрес действителен","structure":"Терминал А","addressString":"г. Новосибирск, ул. Станционная, д. 20, стр. 1, платформа 3","existInVspGosbTbRegistry":false,"addressType":"ORDINARY","vsp":null,"gosb":null}'::jsonb,
     '{"contactPerson": "Сергей Лебедев", "contactPhone": "+7 913 444-55-66", "organizationInn": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380044"}'::jsonb),

    -- Заявка 4: UNLOAD
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380006', 1, 'UNLOAD',
     '2026-02-07'::DATE,
     '12:00:00'::TIME,
     '14:00:00'::TIME,
     500,
     '{"latitude":59.9343,"longitude":30.3351,"country":"Россия","region":"Ленинградская область","city":"Санкт-Петербург","street":"Трамвайный проезд","house":"15","settlement":"Санкт-Петербург","building":"7","district":"Фрунзенский район","livingArea":"Московская застава","place":"Логистический парк","entrance":"Ворота 12","floor":"","flat":"","processInformation":"Адрес проверен","structure":"Парк D","addressString":"г. Санкт-Петербург, Трамвайный пр., д. 15, стр. 7, ворота 12","existInVspGosbTbRegistry":true,"addressType":"VSP","vsp":"СПб Южный","gosb":"СПб ГОСБ"}'::jsonb,
     '{"contactPerson": "Ольга Соколова", "contactPhone": "+7 921 333-44-55", "organizationInn": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380055"}'::jsonb),

    -- Заявка 5: LOAD
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380007', 0, 'LOAD',
     '2026-02-06'::DATE,
     '04:00:00'::TIME,
     '06:00:00'::TIME,
     800,
     '{"latitude":56.8584,"longitude":60.6104,"country":"Россия","region":"Свердловская область","city":"Екатеринбург","street":"Машиностроителей","house":"40","settlement":"Екатеринбург","building":"8","district":"Орджоникидзевский район","livingArea":"Уралмаш","place":"Производственная зона","entrance":"Цех 5","floor":"","flat":"","processInformation":"Адрес подтверждён","structure":"Здание 8","addressString":"г. Екатеринбург, ул. Машиностроителей, д. 40, стр. 8, цех 5","existInVspGosbTbRegistry":false,"addressType":"ORDINARY","vsp":null,"gosb":null}'::jsonb,
     '{"contactPerson": "Андрей Фролов", "contactPhone": "+7 922 666-77-88", "organizationInn": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380044"}'::jsonb),

    -- Заявка 5: UNLOAD
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380007', 1, 'UNLOAD',
     '2026-02-08'::DATE,
     '13:00:00'::TIME,
     '15:00:00'::TIME,
     600,
     '{"latitude":55.7558,"longitude":37.6173,"country":"Россия","region":"Московская область","city":"Москва","street":"Волгоградский проспект","house":"100","settlement":"Москва","building":"15","district":"Текстильщики","livingArea":"Кузьминки","place":"Торговый центр","entrance":"Подъезд C","floor":"2","flat":"205","processInformation":"Адрес распознан","structure":"Блок C","addressString":"г. Москва, Волгоградский пр., д. 100, стр. 15, подъезд C, этаж 2, кв. 205","existInVspGosbTbRegistry":true,"addressType":"VSP","vsp":"Московский Восточный","gosb":"Московский ГОСБ"}'::jsonb,
     '{"contactPerson": "Наталья Морозова", "contactPhone": "+7 916 888-99-00", "organizationInn": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380055"}'::jsonb),
-- Заявка 6: LOAD (ОП-202602-0000006)
(gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380008', 0, 'LOAD',
 '2026-02-07'::DATE,
 '08:00:00'::TIME,
 '10:00:00'::TIME,
 500,
 '{"latitude":55.7558,"longitude":37.6173,"country":"Россия","region":"Московская область","city":"Москва","street":"Новгородская ул.","house":"34","settlement":"Москва","building":"2","district":"Юго-Западный округ","livingArea":"Обручевский","place":"Складской комплекс","entrance":"Въезд А","floor":"","flat":"","processInformation":"Адрес подтверждён","structure":"Блок 5","addressString":"г. Москва, ул. Новгородская, д. 34, стр. 2, въезд А","existInVspGosbTbRegistry":false,"addressType":"ORDINARY","vsp":null,"gosb":null}'::jsonb,
 '{"contactPerson": "Анна Козлова", "contactPhone": "+7 916 222-33-44", "organizationInn": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380044"}'::jsonb),

-- Заявка 6: UNLOAD (ОП-202602-0000006)
(gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380008', 1, 'UNLOAD',
 '2026-02-09'::DATE,
 '10:00:00'::TIME,
 '12:00:00'::TIME,
 400,
 '{"latitude":59.9343,"longitude":30.3351,"country":"Россия","region":"Ленинградская область","city":"Санкт-Петербург","street":"Транспортный пр.","house":"12","settlement":"Санкт-Петербург","building":"7","district":"Фрунзенский район","livingArea":"Московская застава","place":"Логистический парк","entrance":"Ворота 3","floor":"","flat":"","processInformation":"Адрес проверен","structure":"Секция B","addressString":"г. Санкт-Петербург, Транспортный пр., д. 12, стр. 7, ворота 3","existInVspGosbTbRegistry":true,"addressType":"VSP","vsp":"СПб Южный","gosb":"СПб ГОСБ"}'::jsonb,
 '{"contactPerson": "Дмитрий Смирнов", "contactPhone": "+7 921 444-55-66", "organizationInn": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380055"}'::jsonb),

-- Заявка 9: LOAD (ОП-202602-0000009)
(gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380011', 0, 'LOAD',
 '2026-02-10'::DATE,
 '07:00:00'::TIME,
 '09:00:00'::TIME,
 600,
 '{"latitude":54.7114,"longitude":20.4880,"country":"Россия","region":"Калининградская область","city":"Калининград","street":"Советский проспект","house":"25","settlement":"Калининград","building":"1","district":"Центральный округ","livingArea":"Центр","place":"Оптовый склад","entrance":"Въезд со двора","floor":"","flat":"","processInformation":"Адрес подтверждён","structure":"Склад 3","addressString":"г. Калининград, Советский пр., д. 25, стр. 1, въезд со двора","existInVspGosbTbRegistry":false,"addressType":"ORDINARY","vsp":null,"gosb":null}'::jsonb,
 '{"contactPerson": "Елена Михайлова", "contactPhone": "+7 910 555-66-77", "organizationInn": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380044"}'::jsonb),

-- Заявка 9: UNLOAD (ОП-202602-0000009)
(gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380011', 1, 'UNLOAD',
 '2026-02-12'::DATE,
 '11:00:00'::TIME,
 '13:00:00'::TIME,
 500,
 '{"latitude":55.7558,"longitude":37.6173,"country":"Россия","region":"Московская область","city":"Москва","street":"Ленинский проспект","house":"100","settlement":"Москва","building":"10","district":"Гагаринский район","livingArea":"Ломоносово","place":"Торговый центр","entrance":"Подъезд B","floor":"1","flat":"101","processInformation":"Адрес распознан","structure":"Корпус Б","addressString":"г. Москва, Ленинский пр., д. 100, стр. 10, подъезд B, этаж 1, кв. 101","existInVspGosbTbRegistry":true,"addressType":"VSP","vsp":"Московский Центральный Узел","gosb":"Московский ГОСБ"}'::jsonb,
 '{"contactPerson": "Игорь Петров", "contactPhone": "+7 916 666-77-88", "organizationInn": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380055"}'::jsonb);

-- Вставка откликов перевозчиков (request_carrier_reply), связанных с заявками 2, 3, 8
INSERT INTO exchange_request.request_carrier_reply (
    id,
    request_id,
    organization_id,
    reply,
    created_at
) VALUES

    -- Отклик на заявку 4 (CONFIRMED)
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380006', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380088',
     '{
       "auto": {
         "model": "DAF XF 105",
         "mark": "DAF",
         "regNumber": "К678АР777"
       },
       "driver": {
         "fio": "Сергеев Николай Алексеевич",
         "phone": "+7 926 111-22-33",
         "licence": "7773456789"
       },
       "cost": 1800000.00,
       "vat": "VAT_20",
       "comment": "Есть опыт перевозки техники"
     }'::JSONB,
     NOW() - INTERVAL '3 days'
    ),

    -- Отклик на заявку 5 (CONFIRMED)
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380007', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380089',
     '{
       "auto": {
         "model": "MAN TGX",
         "mark": "MAN",
         "regNumber": "М123КТ888"
       },
       "driver": {
         "fio": "Васильев Дмитрий Сергеевич",
         "phone": "+7 915 444-55-66",
         "licence": "8881234567"
       },
       "cost": 950000.00,
       "vat": "VAT_20",
       "comment": "Готов к отправке завтра"
     }'::JSONB,
     NOW() - INTERVAL '2 days'
    ),

    -- Отклик на заявку 6 (PUBLISHED)
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380008', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380088',
     '{
       "auto": {
         "model": "IVECO Stralis",
         "mark": "IVECO",
         "regNumber": "И901НУ777"
       },
       "driver": {
         "fio": "Кузнецов Андрей Викторович",
         "phone": "+7 921 777-88-99",
         "licence": "7779012345"
       },
       "cost": 1300000.00,
       "vat": "VAT_20",
       "comment": "Имеем рефрижератор"
     }'::JSONB,
     NOW() - INTERVAL '1 day'
    ),

    -- Отклик на заявку 7 (PUBLISHED)
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380009', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380090',
     '{
       "auto": {
         "model": "Mercedes-Benz Actros",
         "mark": "Mercedes",
         "regNumber": "М234РЕ888"
       },
       "driver": {
         "fio": "Федоров Михаил Петрович",
         "phone": "+7 910 555-66-77",
         "licence": "8885556677"
       },
       "cost": 450000.00,
       "vat": "VAT_20",
       "comment": "Можно догружать"
     }'::JSONB,
     NOW() - INTERVAL '12 hours'
    ),
     (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380011', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380088',
     '{
       "auto": {
         "model": "Scania R730",
         "mark": "Scania",
         "regNumber": "С456ТУ777"
       },
       "driver": {
         "fio": "Лебедев Артём Павлович",
         "phone": "+7 926 333-44-55",
         "licence": "7776667778"
       },
       "cost": 700000.00,
       "vat": "VAT_20",
       "comment": "Опыт перевозки химикатов"
     }'::JSONB,
     NOW() - INTERVAL '10 hours'
    ),

    -- Отклик на заявку 10 (PUBLISHED)
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380012', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380092',
     '{
       "auto": {
         "model": "Камаз-43118",
         "mark": "Камаз",
         "regNumber": "К789ВЕ888"
       },
       "driver": {
         "fio": "Соколов Виктор Олегович",
         "phone": "+7 915 888-99-00",
         "licence": "8889990001"
       },
       "cost": 1100000.00,
       "vat": "VAT_20",
       "comment": "Готов к работе в выходные"
     }'::JSONB,
     NOW() - INTERVAL '8 hours'
    ),

    -- Отклик на заявку 6 от другого перевозчика
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380008', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380093',
     '{
       "auto": {
         "model": "Renault Magnum",
         "mark": "Renault",
         "regNumber": "Р123АК999"
       },
       "driver": {
         "fio": "Волков Илья Никитич",
         "phone": "+7 910 222-33-44",
         "licence": "9991112223"
       },
       "cost": 1250000.00,
       "vat": "VAT_20",
       "comment": "Низкая ставка, но надёжно"
     }'::JSONB,
     NOW() - INTERVAL '7 hours'
    ),

    -- Отклик на заявку 3 от другого перевозчика
    (gen_random_uuid(), 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380005', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380094',
     '{
       "auto": {
         "model": "MAZ-6440",
         "mark": "МАЗ",
         "regNumber": "М456СР178"
       },
       "driver": {
         "fio": "Григорьев Станислав Михайлович",
         "phone": "+7 921 444-55-66",
         "licence": "1784567890"
       },
       "cost": 700000.00,
       "vat": "VAT_20",
       "comment": "Быстрая погрузка, опыт доставки одежды"
     }'::JSONB,
     NOW() - INTERVAL '6 hours'
    );
