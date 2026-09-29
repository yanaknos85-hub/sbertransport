
-- 2. Вставка: Организация грузовладельца
INSERT INTO exchange_request.organization (
    id,
    name,
    inn,
    kpp,
    legal_address,
    bank_account,
    bank_name,
    bic
) VALUES (
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004',
    'ООО "Транспортная Грузовладельческая Компания "Властилинофф"',
    '7701234567',
    '770101001',
    '115088, г. Москва, ул. Югорская, д. 43',
    '40702810538000000001',
    'ПАО Сбербанк',
    '044525225'
);
--  Вставка: Организация грузоперевозчик
INSERT INTO exchange_request.organization (
    id,
    name,
    inn,
    kpp,
    legal_address,
    bank_account,
    bank_name,
    bic
) VALUES (
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380088',
    'ООО "Транспортная Компания "Грузовичкофф"',
    '7701234567',
    '770101001',
    '115088, г. Москва, ул. Южнопортовая, д. 15',
    '40702810538000000001',
    'ПАО Сбербанк',
    '044525225'
);


-- 3. Вставка: Пользователь
INSERT INTO exchange_request.users (
    id,
    email,
    phone,
    organization_id
) VALUES (
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380005',
    'manager@gruzovichkoff.ru',
    null,
--    '+7 916 123-45-67',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004'
);
-- 3. Вставка: Пользователь
INSERT INTO exchange_request.users (
    id,
    email,
    phone,
    organization_id
) VALUES (
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380007',
    'manager2@gruzovichkoff.ru',
    null,
--    '+7 916 123-45-67',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004'
);
-- Вставка: Пользователь Грузоперевозчика
INSERT INTO exchange_request.users (
    id,
    email,
    phone,
    organization_id
) VALUES (
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380023',
    'manager3@gruzovichkoff.ru',
    null,
--    '+7 916 123-45-67',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380088'
);