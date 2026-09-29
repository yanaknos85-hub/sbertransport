INSERT INTO vehicle.vehicle (id, model_id, category_id, manufacturer, ecological_class, engine_power, engine_capacity,
                             fuel_tank_volume, drive_id, mudguard_installed, spare_wheel_holder_installed,
                             weight, max_weight, height, width, length, service_interval_days, service_interval_mileage,
                             service_authorization_days, service_authorization_mileage, body_type_id, transmission_type_id,
                             front_wheel_size_id, rear_wheel_size_id, year_manufacture_begin, year_manufacture_end, engine_type_id)
VALUES ('748ba8ab-572a-4178-9e05-fae61aefb376', 'c8489954-548b-4a44-a621-aabce28da500',
        'be8abe82-1cbd-4f0e-9b31-2b9db90231df', 'ВАЗ', '4', 240.33, 1400, 50,
        '409570dc-355c-4423-8f53-8ef1f945e345', true, false, 700, 900, 1400, 1650, 4122, 10000, 180, 10, 2000,
        '16e717da-d462-4d22-b5d2-fbfdcc489445', '4fcd92db-415c-40b2-ada6-63d9b9409c2a',
        'b7a63d86-9afe-44b6-b672-123d11bb8ae8', 'b7a63d86-9afe-44b6-b672-123d11bb8ae8', 1980, null, '773b5013-ac57-45e9-9d0b-75221c3ce333'),
       ('748ba8ab-572a-4178-9e05-fae61aefb377', 'c8489954-548b-4a44-a621-aabce28da500',
        'be8abe82-1cbd-4f0e-9b31-2b9db90231df', 'ВАЗ', '4', 240.33, 1400, 50,
        'fe07b7eb-7aa0-4ad0-ae43-d91b8d85d632', true, false, 700, 900, 1400, 1650, 4122, 10000, 180, 10, 2000,
        '16e717da-d462-4d22-b5d2-fbfdcc489445', '4fcd92db-415c-40b2-ada6-63d9b9409c2a',
        'b7a63d86-9afe-44b6-b672-123d11bb8ae8', 'b7a63d86-9afe-44b6-b672-123d11bb8ae8', 1970, 2023, '2717f985-4a59-403f-92a2-ba0e20de20a8'),
       ('400466cb-df0e-40be-8b34-9ff42444637a', '1ba85b47-c1fb-4f36-a9b2-762426fc1fce',
        'be8abe82-1cbd-4f0e-9b31-2b9db90231df', 'Changan', '5', 9999.99, 2500, 90,
        'fe07b7eb-7aa0-4ad0-ae43-d91b8d85d632', false, true, 1100, 1300, 1600, 1850, 4522, 10000, 180, 10, 2000,
        'b0a5f7e7-8c63-487e-a452-3bb617a29faa', 'b4ec0b58-d1b3-45b0-ad36-0cf0bfd50a0a',
        '1fd0a13d-6bd9-4c8c-8bc9-cd029034d4df', '1fd0a13d-6bd9-4c8c-8bc9-cd029034d4df', 2021, 2024, '2717f985-4a59-403f-92a2-ba0e20de20a8'),
       ('400466cb-df0e-40be-8b34-9ff42444637b', '1ba85b47-c1fb-4f36-a9b2-762426fc1fce',
        'be8abe82-1cbd-4f0e-9b31-2b9db90231df', 'Changan', '5', 9999.99, 2500, 90,
        '409570dc-355c-4423-8f53-8ef1f945e345', false, true, 1100, 1300, 1600, 1850, 4522, 10000, 180, 10, 2000,
        'b0a5f7e7-8c63-487e-a452-3bb617a29faa', 'b4ec0b58-d1b3-45b0-ad36-0cf0bfd50a0a',
        '1fd0a13d-6bd9-4c8c-8bc9-cd029034d4df', '1fd0a13d-6bd9-4c8c-8bc9-cd029034d4df', 2010, null, '773b5013-ac57-45e9-9d0b-75221c3ce333');

INSERT INTO vehicle_fuel_type (vehicle_id, fuel_type_id)
VALUES ('748ba8ab-572a-4178-9e05-fae61aefb376', '67a89599-fa56-4329-b671-981f78883319'),
       ('748ba8ab-572a-4178-9e05-fae61aefb377', 'e705ffd8-f158-4455-a005-5ea644614223'),
       ('400466cb-df0e-40be-8b34-9ff42444637a', 'e705ffd8-f158-4455-a005-5ea644614223'),
       ('400466cb-df0e-40be-8b34-9ff42444637b', '67a89599-fa56-4329-b671-981f78883319');

