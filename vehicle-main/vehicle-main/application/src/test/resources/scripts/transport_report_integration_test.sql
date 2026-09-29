INSERT INTO accessible_position (id, title) values ('60b9b87e-3a4c-4500-acc4-60dbea5eec46', 'Доступная позиция') ON CONFLICT DO NOTHING;

INSERT INTO vehicle.transport (id, inventory_number, asset_number, vehicle_id, subtype_id, state_number, vin_code,
                               chassis_number, body_number, certificate_number, certificate_issued_date, passport_number, passport_issued_date,
                               brand_by_passport, model_by_passport, body_color, telematics_id, exploitation_start, exploitation_end, current_mileage,
                               status, year, vehicle_type, location_address, parking_address, comment, balance_unit_number, facility, equipment_unit_system_number, accessible_position_id)
VALUES ('bc3f3b07-f136-4270-af0b-ff84af381ac4', '6', '6', '748ba8ab-572a-4178-9e05-fae61aefb376', 'c5964d64-7b98-4978-bf93-3c48d38c3d7c',
        'А111АА116', '6111sssssssss', '6', '6', '6', '2023-03-20', '6', '2024-03-05', '6', '6', '6', '60b9b87e-3a4c-4500-acc4-60dbea5eec46',
        '2023-03-01', null, 6, 'IN_USE', 2007, 'Легковой', 'Москва', 'Москва', 'НЕТ', '0', '0', '0', '60b9b87e-3a4c-4500-acc4-60dbea5eec46');

INSERT INTO vehicle.transport_department VALUES ('bc3f3b07-f136-4270-af0b-ff84af381ac4', '003a33fb-faa6-49a7-be37-106fdbef2324');
INSERT INTO vehicle.transport_organization VALUES ('bc3f3b07-f136-4270-af0b-ff84af381ac4', 'fc73b25b-9564-4560-98b5-abc0f16af9b2');

update vehicle.vehicle set year_manufacture_end = '2035' where id = '748ba8ab-572a-4178-9e05-fae61aefb376';

-- EMPLOYEE_2
INSERT INTO vehicle.organization (id, digit_id, active, official_name)
VALUES ('d22eb5b9-d45c-4acf-a878-562c3287df5d', 40, true, 'Дальневосточный банк (ДВБ)'),
       ('5e9b64f0-2a86-4321-b038-931441d5e7f2', 52, true, 'Магаданский банк');
INSERT INTO vehicle.department (id, active, department_name, human_readable_id, organization_id, parent_id, easup_id)
VALUES ('7b2879f8-95b2-4eb4-aadd-d4377170de07', true, 'Отдел трансп обеспечения УРМ г. Магадан уровень 2', 'DT-0040-00001121',
        'd22eb5b9-d45c-4acf-a878-562c3287df5d', null, '10110911');
INSERT INTO vehicle.department (id, active, department_name, human_readable_id, organization_id, parent_id, easup_id)
VALUES ('cd778185-688a-4c55-afa3-f5ffe54eba90', true, 'Отдел трансп обеспечения УРМ г. Магадан', 'DT-0040-00001120',
        'd22eb5b9-d45c-4acf-a878-562c3287df5d', '7b2879f8-95b2-4eb4-aadd-d4377170de07', '10110907');
INSERT INTO vehicle.position (id, active, organization_id, position_name)
VALUES ('832d47fc-e67e-4c4a-9f5e-daaef262c9c8', true, 'd22eb5b9-d45c-4acf-a878-562c3287df5d', 'Сотрудник');
INSERT INTO vehicle.employee (id, active, human_readable_id, first_name, last_name, patronymic, mobile_phone,
                              personnel_number, user_id, department_id, position_id, organization_id)
VALUES ('cc4648a9-dbdd-461f-927f-a6e50ab340f8', true, 'US-0008-00044610', 'Тест', 'Тест', '', '+73123123123',
        '2016497', 'cc4648a9-dbdd-461f-927f-a6e50ab340f8', 'cd778185-688a-4c55-afa3-f5ffe54eba90',
        '832d47fc-e67e-4c4a-9f5e-daaef262c9c8', 'd22eb5b9-d45c-4acf-a878-562c3287df5d');

INSERT INTO vehicle.transport (id, inventory_number, asset_number, vehicle_id, subtype_id, state_number, vin_code,
                               chassis_number, body_number, certificate_number, certificate_issued_date, passport_number, passport_issued_date,
                               brand_by_passport, model_by_passport, body_color, telematics_id, exploitation_start, exploitation_end, current_mileage,
                               status, year, vehicle_type, location_address, parking_address, comment, balance_unit_number, facility, equipment_unit_system_number, accessible_position_id)
VALUES ('bc3f3b07-f136-4270-af0b-ff84af381ac3', '6', '62', '400466cb-df0e-40be-8b34-9ff42444637a', 'c5964d64-7b98-4978-bf93-3c48d38c3d7c',
        'А111АА126', '6111sssssssss1', '6', '6', '6', '2023-03-20','6', '2024-03-05', '6', '6', '6', '60b9b87e-3a4c-4500-acc4-60dbea5eec46',
        '2023-03-01', null, 6, 'IN_USE', 2007, 'Легковой','Москва', 'Москва', 'НЕТ', '0', '0', '0', '60b9b87e-3a4c-4500-acc4-60dbea5eec46');

INSERT INTO vehicle.transport_department
VALUES ('bc3f3b07-f136-4270-af0b-ff84af381ac3', '7b2879f8-95b2-4eb4-aadd-d4377170de07'),
       ('bc3f3b07-f136-4270-af0b-ff84af381ac3', 'cd778185-688a-4c55-afa3-f5ffe54eba90');
INSERT INTO vehicle.transport_organization
VALUES ('bc3f3b07-f136-4270-af0b-ff84af381ac3', 'd22eb5b9-d45c-4acf-a878-562c3287df5d'),
       ('bc3f3b07-f136-4270-af0b-ff84af381ac3', '5e9b64f0-2a86-4321-b038-931441d5e7f2');

 INSERT INTO vehicle.transport (id, inventory_number, asset_number, vehicle_id, subtype_id, state_number, vin_code,
                                chassis_number, body_number, certificate_number, certificate_issued_date, passport_number, passport_issued_date,
                                brand_by_passport, model_by_passport, body_color, telematics_id, exploitation_start, exploitation_end, current_mileage,
                                status, year, vehicle_type, location_address, parking_address, comment, balance_unit_number, facility, equipment_unit_system_number, accessible_position_id,
                                contractor_id, autopark_id)
 VALUES ('bf30c674-9b1e-4f42-b892-774c52a04a31', '6', '63', '400466cb-df0e-40be-8b34-9ff42444637a', 'c5964d64-7b98-4978-bf93-3c48d38c3d7c',
         'А111АА136', '6111sssssssss2', '6', '6', '6', '2023-03-20','6', '2024-03-05', '6', '6', '6', '60b9b87e-3a4c-4500-acc4-60dbea5eec46',
         '2023-03-01', null, 6, 'IN_USE', 2007, 'Легковой','Москва', 'Москва', 'НЕТ', '0', '0', '0', '60b9b87e-3a4c-4500-acc4-60dbea5eec46',
         '8f4a5468-38dd-493f-8be4-dd930056f80e', '90ef9bca-beeb-45af-b8c7-121204e67f15');