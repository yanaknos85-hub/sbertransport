INSERT INTO vehicle.organization (id, digit_id, active, official_name)
VALUES ('fc73b25b-9564-4560-98b5-abc0f16af9b2', 40, true, 'Дальневосточный банк (ДВБ)');
INSERT INTO vehicle.department (id, active, department_name, human_readable_id, organization_id, parent_id, easup_id)
VALUES ('d728e86f-d624-fbf7-3fe6-fa07f3ead218', true, 'Отдел трансп обеспечения УРМ г. Магадан уровень 2', 'DT-0040-00001121',
        'fc73b25b-9564-4560-98b5-abc0f16af9b2', null, '10110911');
INSERT INTO vehicle.department (id, active, department_name, human_readable_id, organization_id, parent_id, easup_id)
VALUES ('003a33fb-faa6-49a7-be37-106fdbef2324', true, 'Отдел трансп обеспечения УРМ г. Магадан', 'DT-0040-00001120',
        'fc73b25b-9564-4560-98b5-abc0f16af9b2', 'd728e86f-d624-fbf7-3fe6-fa07f3ead218', '10110907');
INSERT INTO vehicle.position (id, active, organization_id, position_name)
VALUES ('f810b517-bbee-43e2-82ad-3f0cd10985ed', true, 'fc73b25b-9564-4560-98b5-abc0f16af9b2', 'Сотрудник');
INSERT INTO vehicle.employee (id, active, human_readable_id, first_name, last_name, patronymic, mobile_phone,
                              personnel_number, user_id, department_id, position_id, organization_id)
VALUES ('3cd35c19-fd39-413c-99a0-30f35bd642a7', true, 'US-0008-00044610', 'Тест', 'Тест', '', '+73123123123',
        '2016497', '3cd35c19-fd39-413c-99a0-30f35bd642a7', '003a33fb-faa6-49a7-be37-106fdbef2324',
        'f810b517-bbee-43e2-82ad-3f0cd10985ed', 'fc73b25b-9564-4560-98b5-abc0f16af9b2');
INSERT INTO vehicle.employee (id, active, human_readable_id, first_name, last_name, patronymic, mobile_phone,
                              personnel_number, user_id, department_id, position_id, organization_id)
VALUES ('3cd35c19-fd39-413c-99a0-30f35bd642a8', true, 'US-0008-00044610', 'Тест', 'Тест', '', '+73123123123',
        '2016497', '3cd35c19-fd39-413c-99a0-30f35bd642a8', 'd728e86f-d624-fbf7-3fe6-fa07f3ead218',
        'f810b517-bbee-43e2-82ad-3f0cd10985ed', 'fc73b25b-9564-4560-98b5-abc0f16af9b2');

INSERT INTO vehicle.organization (id, digit_id, active, official_name)
VALUES ('3cbe0b6b-fe04-4a28-83e8-184f0f331bff', 47, true, 'Московский банк');
INSERT INTO vehicle.department (id, active, department_name, human_readable_id, organization_id, parent_id, easup_id)
VALUES ('000098ba-5c12-423f-ba6a-de9c76db31a5', true, 'С по ОФЛ ДО № 9038/01641', 'DT-0047-00000441',
        '3cbe0b6b-fe04-4a28-83e8-184f0f331bff', null, null);
INSERT INTO vehicle.position (id, active, organization_id, position_name)
VALUES ('3db788d4-6111-45e9-b537-40f5586cf49a', true, '3cbe0b6b-fe04-4a28-83e8-184f0f331bff', 'Сотрудник');
INSERT INTO vehicle.employee (id, active, human_readable_id, first_name, last_name, patronymic, mobile_phone,
                              personnel_number, user_id, department_id, position_id, organization_id)
VALUES ('01fab58c-ac99-45ef-a83e-f05b5a39d88d', true, 'US-0009-00044611', 'Тест2', 'Тест2', '', '+73123123124',
        '2016590', '01fab58c-ac99-45ef-a83e-f05b5a39d88d', '000098ba-5c12-423f-ba6a-de9c76db31a5',
        '3db788d4-6111-45e9-b537-40f5586cf49a', '3cbe0b6b-fe04-4a28-83e8-184f0f331bff');

INSERT INTO vehicle.type (id, title)
VALUES ('66498404-6a81-4c58-a4bd-af35fac627a4', 'Запасной');
INSERT INTO vehicle.subtype (id, title, type_id)
VALUES ('c5964d64-7b98-4978-bf93-3c48d38c3d7c', 'На всякий', '66498404-6a81-4c58-a4bd-af35fac627a4');
INSERT INTO vehicle.telematics (id, imei, title)
VALUES ('60b9b87e-3a4c-4500-acc4-60dbea5eec46', '3433435', 'Santel');
INSERT INTO vehicle.vehicle (id, model_id, category_id, manufacturer, ecological_class, engine_power, engine_capacity,
                             fuel_tank_volume, drive_id, mudguard_installed, spare_wheel_holder_installed,
                             weight, max_weight, height, width, length, service_interval_days, service_interval_mileage,
                             service_authorization_days, service_authorization_mileage, body_type_id,
                             transmission_type_id, front_wheel_size_id, rear_wheel_size_id, year_manufacture_begin, year_manufacture_end,
                             engine_type_id, city_consumption_rate, country_consumption_rate, hybrid_consumption_rate)
VALUES ('748ba8ab-572a-4178-9e05-fae61aefb376', 'c8489954-548b-4a44-a621-aabce28da500',
        'be8abe82-1cbd-4f0e-9b31-2b9db90231df', 'ВАЗ', '4', 240.33, 1400, 50,
        '409570dc-355c-4423-8f53-8ef1f945e345', true, false, 700, 900, 1400, 1650, 4122, 10000, 180, 10, 2000,
        '16e717da-d462-4d22-b5d2-fbfdcc489445', '4fcd92db-415c-40b2-ada6-63d9b9409c2a',
        '1fd0a13d-6bd9-4c8c-8bc9-cd029034d4df',
        '1fd0a13d-6bd9-4c8c-8bc9-cd029034d4df', 1980, null, '773b5013-ac57-45e9-9d0b-75221c3ce333', 10, 10, 10),
        ('748ba8ab-572a-4178-9e05-fae61aefb371', 'c8489954-548b-4a44-a621-aabce28da502',
                'be8abe82-1cbd-4f0e-9b31-2b9db90231df', 'ВАЗ', '4', 240.33, 1400, 50,
                '409570dc-355c-4423-8f53-8ef1f945e345', true, false, 700, 900, 1400, 1650, 4122, 10000, 180, 10, 2000,
                '16e717da-d462-4d22-b5d2-fbfdcc489445', '4fcd92db-415c-40b2-ada6-63d9b9409c2a',
                '1fd0a13d-6bd9-4c8c-8bc9-cd029034d4df',
                '1fd0a13d-6bd9-4c8c-8bc9-cd029034d4df', 1980, null, '773b5013-ac57-45e9-9d0b-75221c3ce333', 10, 10, 10),
       ('400466cb-df0e-40be-8b34-9ff42444637a', '1ba85b47-c1fb-4f36-a9b2-762426fc1fce',
        'be8abe82-1cbd-4f0e-9b31-2b9db90231df', 'Changan', '5', 9999.99, 1400, 90,
        'fe07b7eb-7aa0-4ad0-ae43-d91b8d85d632', false, true, 1100, 1300, 1600, 1850, 4522, 10000, 180, 10, 2000,
        'b0a5f7e7-8c63-487e-a452-3bb617a29faa', 'b4ec0b58-d1b3-45b0-ad36-0cf0bfd50a0a',
        'b7a63d86-9afe-44b6-b672-123d11bb8ae8',
        'b7a63d86-9afe-44b6-b672-123d11bb8ae8', 2023, 2024, '2717f985-4a59-403f-92a2-ba0e20de20a8', 10, 10, 10);

INSERT INTO vehicle.vehicle_fuel_type (vehicle_id, fuel_type_id)
VALUES ('748ba8ab-572a-4178-9e05-fae61aefb376', '67a89599-fa56-4329-b671-981f78883319'),
       ('400466cb-df0e-40be-8b34-9ff42444637a', 'e705ffd8-f158-4455-a005-5ea644614223'),
       ('748ba8ab-572a-4178-9e05-fae61aefb376', 'e705ffd8-f158-4455-a005-5ea644614223');
