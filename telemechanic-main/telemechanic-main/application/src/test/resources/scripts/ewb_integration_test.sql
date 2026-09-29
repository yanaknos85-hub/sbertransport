INSERT INTO telemechanic.organization (id, digit_id, active, official_name, msrn, tin)
VALUES ('dd89fad5-7c0e-4eaf-9483-de442e40f159', 12, true, 'Новая организация', '12345678', '123456');
INSERT INTO telemechanic.position (id, active, organization_id, position_name)
VALUES ('257d43ac-dc52-4dee-9346-580ba442d677', true, 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', 'Не_ожидает_обновления'),
       ('00204b7d-c658-44b5-b2c5-33abd916d0ac', true, 'dd89fad5-7c0e-4eaf-9483-de442e40f159', 'Другая должность');
INSERT INTO telemechanic.department (id, active, department_name, human_readable_id, easup_id, organization_id,
                                     parent_id, autopark_id)
VALUES ('20d4a338-e121-4a0b-9d80-a7b6b035484f', true, 'Группа разработки', 'DT-0008-1468', '10288394',
        'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', null, '06d789cf-fac8-46ae-889b-3e15e111c757'),
       ('d640b449-4482-4a48-bc05-2bbcfa66b465', true, 'Группа разработки', 'DT-0008-764', '10289556',
        'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', null, null),
       ('ccb34fc5-0351-469b-b26a-7085fc9f73e0', true, 'Группа разработки другой организации', 'DT-0008-1469', '10288395',
        'dd89fad5-7c0e-4eaf-9483-de442e40f159', null, null);
INSERT INTO telemechanic.employee (id, active, human_readable_id, first_name, last_name, patronymic, mobile_phone,
                                   personnel_number, user_id, department_id, position_id, organization_id,
                                   full_name_index)
VALUES ('40b80a64-a191-46c6-bbbb-16a60e772f7f', true, 'US-0008-00026594', 'Василий', 'Клюнков', 'Александрович', null,
        '1806769', '40b80a64-a191-46c6-bbbb-16a60e772f7f', '20d4a338-e121-4a0b-9d80-a7b6b035484f',
        '257d43ac-dc52-4dee-9346-580ba442d677', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', 'КлюнковВасилийАлександрович'),
        ('95c2bd4f-3a79-4b30-8d7e-9308e751b40a', true, 'US-0008-39344', 'Максим', 'Макаров', 'Дмитриевич', null,
        '1913586', '95c2bd4f-3a79-4b30-8d7e-9308e751b40a', 'd640b449-4482-4a48-bc05-2bbcfa66b465',
        '257d43ac-dc52-4dee-9346-580ba442d677', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', 'МакаровМаксимДмитриевич'),
        ('6deae338-2402-4a04-a85f-e4f242eb357e', true, 'US-0008-39344', 'Алексей', 'Скакун', null, null,
        '1913587', '6deae338-2402-4a04-a85f-e4f242eb357e', 'd640b449-4482-4a48-bc05-2bbcfa66b465',
        '257d43ac-dc52-4dee-9346-580ba442d677', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', 'СкакунАлексей'),
        ('34ed64fe-2e2e-4eb1-96c3-5d359e09a3aa', true, 'US-0008-39345', 'Иван', 'Танин', 'Васильевич', null,
        '1913589', '34ed64fe-2e2e-4eb1-96c3-5d359e09a3aa','ccb34fc5-0351-469b-b26a-7085fc9f73e0',
        '00204b7d-c658-44b5-b2c5-33abd916d0ac', 'dd89fad5-7c0e-4eaf-9483-de442e40f159', 'ИванТанинВасильевич'),
        ('019ff061-c0dd-708a-ac10-787b06956003', true, 'US-0008-41725', 'Юрий', 'Савченко', 'Александрович', null,
        '1973505', '019ff061-c0dd-708a-ac10-787b06956003', '20d4a338-e121-4a0b-9d80-a7b6b035484f',
        '257d43ac-dc52-4dee-9346-580ba442d677', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', 'СавченкоЮрийАлександрович');

INSERT INTO telemechanic.department_time_zone (id, department_id, time_zone)
VALUES ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', '20d4a338-e121-4a0b-9d80-a7b6b035484f', 'UTC+11:00');

insert into telemechanic.organization_medical_license (
    id, series, number, active, issue_date, expiry_date
)
values (
    'd8686e9a-a50c-4c6f-a581-e01ef501e983', '55555', '666666', true, CURRENT_DATE, CURRENT_DATE + INTERVAL '1 YEAR'
);

insert into telemechanic.fleet_owner_organization (organization_id, edf_operator_id, edf_code, active)
VALUES ('cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', '2AE', '444444', true);

insert into telemechanic.ewb_contract
(contract_id, organization_id, inspection_type, edf_operator_id, edf_code, organization_medical_license_id, active, start, "end")
values
('21bfb1f4-6099-45e8-8d81-c42df5070763', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', 'MEDIC', '2BM', '99999',
        'd8686e9a-a50c-4c6f-a581-e01ef501e983', true, current_date, current_date + interval '1 year'),
('e931aa12-0668-4bac-856c-c3d851882911', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', 'TECHNIC', '2AE', '77777',
        null, true, current_date, current_date + interval '1 year');

insert into telemechanic.ewb_tariff
(tariff_id, contract_id, organization_id, department_id, active)
values
('b7557d3f-a094-4fe9-ad31-14d08c7253c7', '21bfb1f4-6099-45e8-8d81-c42df5070763', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6',
    '20d4a338-e121-4a0b-9d80-a7b6b035484f', true),
('aaa4d68c-e94f-4831-9761-3d776894d108', 'e931aa12-0668-4bac-856c-c3d851882911', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6',
    '20d4a338-e121-4a0b-9d80-a7b6b035484f', true);

insert into telemechanic.attorney (id, number, issue_date, expiry_date, creation_system)
values ('fc1bc338-ecf3-4113-b862-d9bb9d8779c4', '76a3d37e-636c-4a66-8782-6199fe002f25', '2020-01-01', '2030-01-01', 'sys'),
       ('b1e0a979-f4d9-4b01-905b-75b9e5f08a30', 'e723d64b-7a2c-4a74-a475-ce9978f6c2c5', '2020-01-01', '2030-01-01', 'sys'),
       ('eee8b35a-4229-42ef-b029-fea007c42cad', '015f7321-5a2a-4ecd-8f11-3a04ceb159d3', '2000-01-01', '2000-01-02', 'sys');

insert into telemechanic.dispatcher (id, employee_id, organization_id, department_id, attorney_id, active)
values ('335f2821-6577-4376-bb29-895c81232f28', '40b80a64-a191-46c6-bbbb-16a60e772f7f', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6',
        '482e6dcb-03a9-4927-90b4-c7081114a9d8', 'eee8b35a-4229-42ef-b029-fea007c42cad', false),
       ('983e4049-e4ac-4639-a782-5a266f4321a8', '40b80a64-a191-46c6-bbbb-16a60e772f7f', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6',
        '20d4a338-e121-4a0b-9d80-a7b6b035484f', 'fc1bc338-ecf3-4113-b862-d9bb9d8779c4', true),
       ('0975c771-059c-4705-aafd-57b6748ff527', '7dd56ea0-fa38-400d-93a6-2a4ef4b7df70', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6',
        '20d4a338-e121-4a0b-9d80-a7b6b035484f', 'b1e0a979-f4d9-4b01-905b-75b9e5f08a30', false);

INSERT INTO telemechanic.driving_license (id, series, number, issue_date, expiry_date, previous_id, active)
VALUES ('e1f2f2a3-fd1b-4088-9c2d-9d36490e08a7', '1234', 1234, '2024-01-01', '2034-01-01', null, true),
       ('8bc3e3a9-d90a-41be-b9eb-af52a26f674e', 's01', 1, '2020-01-01', '2030-01-01', null, false),
       ('66c38d4c-5f88-42c2-9ae1-a5ea36324d4e', 's02', 1, '2023-01-01', '2033-01-01', '8bc3e3a9-d90a-41be-b9eb-af52a26f674e', true),
       ('019ff0a8-f391-7479-8f9a-b8488b35bec8', '2574', '353852', '2024-01-01', '2030-01-01', null, true);

insert into telemechanic.driver (id, employee_id, driving_license_id, tin, snils)
values
('c3b557b7-2921-4ce0-b63e-6ed972975854', '40b80a64-a191-46c6-bbbb-16a60e772f7f', '8bc3e3a9-d90a-41be-b9eb-af52a26f674e', '0000003', '186-345-573 03'),
('167a0b4c-8324-44ba-9619-c0cd583fb1ca', '40b80a64-a191-46c6-bbbb-16a60e772f7f', '66c38d4c-5f88-42c2-9ae1-a5ea36324d4e', '0000003', '186-345-573 03'),
('019ff069-4754-71f2-ab5e-768a795cbc0e', '019ff061-c0dd-708a-ac10-787b06956003', '019ff0a8-f391-7479-8f9a-b8488b35bec8', '0000004', '152-005-380 07');

INSERT INTO telemechanic.tin(id, employee_id, tin)
VALUES ('5aeeee80-1386-4414-8dca-37a6d4857968', '40b80a64-a191-46c6-bbbb-16a60e772f7f', '0987654321'),
       ('f6750ba1-36b1-4d09-a4b4-400edb3e6f16', '95c2bd4f-3a79-4b30-8d7e-9308e751b40a', '1234567890'),
       ('2df00b9f-158c-40a1-99d1-cac7782bed7d', '6deae338-2402-4a04-a85f-e4f242eb357e', '1357924680');

insert into telemechanic.transport (id, state_number, brand, model, status, type, subtype, fuel_tank_volume)
values ('6a897664-9a0f-4e58-b3b7-d666579b37cb', 'А789АА777', 'Classic', 'Lada', 'IN_USE', '-', '-', 40),
       ('17106f98-8d6c-412d-8711-8aeefb04a73b', 'А789АА777', 'Classic', 'Lada', 'IN_USE', '-', '-', 40),
       ('b29ac76a-16c4-49cc-ad7c-f1c719c49f00', 'К742СК724', 'Classic', 'Lada', 'IN_USE', '-', '-', 40);

INSERT INTO telemechanic.transport (id, state_number, brand, model, status, mileage, subtype, type, fuel_tank_volume)
VALUES ('9b1d882c-f623-446b-af1e-ae9ff1966a0e', 'А777АА777', 'Lada', 'Luxe', 'IN_USE', null, 'SUBTYPE', 'TYPE', 40);
INSERT INTO telemechanic.transport_organization (transport_id, organization_id) VALUES ('9b1d882c-f623-446b-af1e-ae9ff1966a0e','cb9f17e7-f658-43ca-a70f-40c1c93ad0a6');

truncate table region cascade;
insert into region values
    ('3cab1b45-d169-40d5-9143-58545f7ea132', '01', 'Первая область'),
    ('0b4e11cf-2eda-4807-87e7-98bec38f262e', '02', 'Вторая область'),
    ('f0977e48-8f0e-4e4b-9595-f52ecc1b5478', '03', 'Третья область');

insert into telemechanic.organization_address (organization_id, zip, region_id)
values ('cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', '123456', '3cab1b45-d169-40d5-9143-58545f7ea132');

INSERT INTO telemechanic.request (id, human_readable_id, author_id, creation_time, transport_id, status, comment,
                                  inspector_id, inspection_time, checks_finished_time, organization_id)
VALUES ('d9d50a1e-1fc4-4458-ade6-f5297304a389', 'OT-0008-00000005', '40b80a64-a191-46c6-bbbb-16a60e772f7f',
        '2023-07-19 16:08:38.858472', '6a897664-9a0f-4e58-b3b7-d666579b37cb', 'IN_PROGRESS', null, null, null, null, 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6');

INSERT INTO telemechanic.request (id, human_readable_id, author_id, creation_time, transport_id, status, comment,
                                  inspector_id, inspection_time, checks_finished_time, organization_id)
VALUES ('d9d51a1e-1fc4-4458-ade6-f5297304a389', 'OT-0008-00000005', '40b80a64-a191-46c6-bbbb-16a60e772f7f',
        '2023-07-19 16:08:38.858472', '6a897664-9a0f-4e58-b3b7-d666579b37cb', 'WARNING', null, null, null, null, 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6');

INSERT INTO telemechanic.request (id, human_readable_id, author_id, creation_time, transport_id, status, comment,
                                  inspector_id, inspection_time, checks_finished_time, organization_id)
VALUES ('019ff050-1d6f-7488-bb5e-f10941c5cfd5', 'TM-0008-00000007', '019ff061-c0dd-708a-ac10-787b06956003',
        '2026-08-11 14:00:27.853271', 'b29ac76a-16c4-49cc-ad7c-f1c719c49f00', 'IN_PROGRESS', null, null, null, null, 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6');

INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id)
VALUES ('d9d52a1e-1fc4-4458-ade6-f5297304a389', 'DONE', 'VEHICLE_NUMBER', 1, 'd9d50a1e-1fc4-4458-ade6-f5297304a389'),
       ('d9d50a1e-1fc4-4458-ade6-f5297304a399', 'IN_PROGRESS', 'OIL_LEVEL', 0, 'd9d50a1e-1fc4-4458-ade6-f5297304a389'),
       ('d9d51a1e-1fc4-4458-ade6-f5297304a389', 'IN_PROGRESS', 'ODOMETER', 0, 'd9d50a1e-1fc4-4458-ade6-f5297304a389'),
       ('76bab9ad-3e0e-479d-969e-3d8f206bd0b6', 'IN_PROGRESS', 'LITREAGE', 0, 'd9d50a1e-1fc4-4458-ade6-f5297304a389'),
       ('d9d51a3e-1fc4-4458-ade6-f5297304a389', 'IN_PROGRESS', 'SPLASH_GUARDS_LR', 0, 'd9d50a1e-1fc4-4458-ade6-f5297304a389');

INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id)
VALUES ('d9d52a1e-1fc4-4458-ade6-f5297314a389', 'DONE', 'VEHICLE_NUMBER', 1, 'd9d51a1e-1fc4-4458-ade6-f5297304a389'),
       ('d9d50a1e-1fc4-4458-ade6-f5297324a399', 'IN_PROGRESS', 'OIL_LEVEL', 0, 'd9d51a1e-1fc4-4458-ade6-f5297304a389'),
       ('d9d51a1e-1fc4-4458-ade6-f5297334a389', 'IN_PROGRESS', 'ODOMETER', 0, 'd9d51a1e-1fc4-4458-ade6-f5297304a389'),
       ('d9d51a3e-1fc4-4458-ade6-f5297354a389', 'IN_PROGRESS', 'SPLASH_GUARDS_LR', 0, 'd9d51a1e-1fc4-4458-ade6-f5297304a389');

INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment)
VALUES ('019ff04d-1a53-7264-9f35-18f8edb1b7d0', 'DONE', 'VEHICLE_NUMBER', 1, '019ff050-1d6f-7488-bb5e-f10941c5cfd5', null),
       ('019ff04d-3ca1-7501-96f4-103dde8fd141', 'DECLINE', 'OIL_LEVEL', 0, '019ff050-1d6f-7488-bb5e-f10941c5cfd5', 'нечеткое фото'),
       ('019ff04d-5b5b-727e-a316-147abf15c5aa', 'IN_PROGRESS', 'ODOMETER', 0, '019ff050-1d6f-7488-bb5e-f10941c5cfd5', null);

INSERT INTO telemechanic.medic_request(id, human_readable_id, creation_time, status, syst_pressure, dyast_pressure, pulse, temperature, blood_alcohol,
                                       comment, organization_id)
VALUES ('6c157938-3604-42a0-bc86-f16526031d5b', 'TL-0000-00000000', '2024-07-31 10:11:39.000000', 'IN_PROGRESS', 120, 80, 60, 36.6, 0.5, 'drunk', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'),
       ('75542a8a-b815-43d6-9a33-63046b19f030', 'TL-0000-00000001', '2024-07-31 10:11:39.000000', 'DONE', 120, 80, 60, 36.6, 0.5, 'drunk', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'),
       ('5456bbff-1f45-4f9a-90d6-85304513d54c', 'TL-0000-00000002', '2024-07-31 10:11:39.000000', 'DECLINED', 120, 80, 60, 36.6, 0.5, 'drunk', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'),
       ('8ad5e9c6-604f-4baa-861f-d7d4c320eb72', 'TL-0000-00000003', '2024-07-31 10:11:39.000000', 'DECLINED', 120, 80, 60, 36.6, 0.5, 'drunk','cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'),
       ('b5a6b2cf-06d3-41f9-9571-43e86ffffa71', 'TL-0008-00000005', '2024-10-10 07:47:11.977', 'IN_PROGRESS', null, null, null, null, null, null, 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6');

INSERT INTO telemechanic.ewb (id, author_id, medic_id, medic_request_id, medic_decision_time, request_id, creation_time, status, human_readable_id,
                              ewb_uuid, start_date, finish_date, organization_id, transport_id, driver_id, driver_license_id, telemech_out_id,
                              telemech_decision_out, telemech_in_id, telemech_decision_in, attorney_out_id, odometer_out, odometer_in, fuel_litreage_out,
                              fuel_litreage_in, tariff_department_id, time_zone, transportation_type, transportation_subtype, communication_type)
VALUES ('140e4734-4909-4bf3-b98f-f24c90d8005f', '95c2bd4f-3a79-4b30-8d7e-9308e751b40a', null, 'b5a6b2cf-06d3-41f9-9571-43e86ffffa71', null,
        'd9d51a1e-1fc4-4458-ade6-f5297304a389',  '2024-07-31 10:11:39.000000', 'ON_THE_LINE', 'EWB_ID1', '140e4734-4909-4bf3-b98f-f24c90d8005f',
        '2023-02-10', '2024-07-31', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', '6a897664-9a0f-4e58-b3b7-d666579b37cb', '167a0b4c-8324-44ba-9619-c0cd583fb1ca',
        'e1f2f2a3-fd1b-4088-9c2d-9d36490e08a7', '95c2bd4f-3a79-4b30-8d7e-9308e751b40a', '2024-07-31 10:11:39.000000', null, '2024-07-31 10:11:39.000000',
        'fc1bc338-ecf3-4113-b862-d9bb9d8779c4', 10000, null, 30, null, '20d4a338-e121-4a0b-9d80-a7b6b035484f', 'UTC+03:00',
        'OWN_ACCOUNT_TRANSPORTATION', 'REGULAR_PASSENGER_TRANSPORTATION', 'URBAN');

INSERT INTO telemechanic.ewb (id, author_id, medic_id, medic_request_id, medic_decision_time, request_id, creation_time, status, human_readable_id,
                              ewb_uuid, start_date, finish_date, organization_id, transport_id, driver_id, driver_license_id, telemech_out_id,
                              telemech_decision_out, telemech_in_id, telemech_decision_in, attorney_out_id, odometer_out, odometer_in, tariff_department_id,
                              time_zone, transportation_type, transportation_subtype, communication_type)
VALUES ('d87dfe9a-5915-4d37-855f-a1598d4819c3', '95c2bd4f-3a79-4b30-8d7e-9308e751b40a', '95c2bd4f-3a79-4b30-8d7e-9308e751b40a',
        '75542a8a-b815-43d6-9a33-63046b19f030', '2024-07-31 10:11:39.000000', 'd9d50a1e-1fc4-4458-ade6-f5297304a389', '2024-07-31 10:11:39.000000',
        'IN_GARAGE', 'EWB_ID2', '240e4734-4909-4bf3-b98f-f24c90d8005f', '2024-07-31', '2024-07-31', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6',
        '6a897664-9a0f-4e58-b3b7-d666579b37cb', '167a0b4c-8324-44ba-9619-c0cd583fb1ca', 'e1f2f2a3-fd1b-4088-9c2d-9d36490e08a7', '34ed64fe-2e2e-4eb1-96c3-5d359e09a3aa',
        '2024-07-31 10:11:39.000000', '34ed64fe-2e2e-4eb1-96c3-5d359e09a3aa', '2024-07-31 10:11:39.000000' , 'fc1bc338-ecf3-4113-b862-d9bb9d8779c4',
        1200, 1000, '20d4a338-e121-4a0b-9d80-a7b6b035484f', 'UTC+03:00', 'COMMERCIAL_TRANSPORTATION', 'PASSENGER_TAXI_TRANSPORTATION', 'SUBURBAN');

INSERT INTO telemechanic.ewb (id, author_id, medic_id, medic_request_id, medic_decision_time, request_id, creation_time, status, human_readable_id,
                              ewb_uuid, start_date, finish_date, organization_id, transport_id, driver_id, driver_license_id, telemech_out_id,
                              telemech_decision_out, telemech_in_id, telemech_decision_in, attorney_out_id, odometer_out, odometer_in, tariff_department_id,
                              time_zone, transportation_type, transportation_subtype, communication_type)
VALUES ('660184ec-a2be-4340-b482-cb57082567a8', '95c2bd4f-3a79-4b30-8d7e-9308e751b40a', '95c2bd4f-3a79-4b30-8d7e-9308e751b40a', '6c157938-3604-42a0-bc86-f16526031d5b',
        '2024-07-31 10:11:39.000000', 'd9d50a1e-1fc4-4458-ade6-f5297304a389', '2024-07-31 10:11:39.000000', 'EWB_CLOSED', 'EWB_ID10', '340e4734-4909-4bf3-b98f-f24c90d8005f',
        '2024-07-31', '2024-07-31', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', '6a897664-9a0f-4e58-b3b7-d666579b37cb', '167a0b4c-8324-44ba-9619-c0cd583fb1ca',
        'e1f2f2a3-fd1b-4088-9c2d-9d36490e08a7', '95c2bd4f-3a79-4b30-8d7e-9308e751b40a', '2024-07-31 10:11:39.000000', '95c2bd4f-3a79-4b30-8d7e-9308e751b40a',
        '2024-07-31 10:11:39.000000' , 'fc1bc338-ecf3-4113-b862-d9bb9d8779c4', 1200, 1000, '20d4a338-e121-4a0b-9d80-a7b6b035484f', 'UTC+03:00',
        'COMMERCIAL_TRANSPORTATION', 'ON_DEMAND_PASSENGER_TRANSPORTATION', 'INTERCITY');

INSERT INTO telemechanic.organization_medical_license(id, series, number, active, issue_date, expiry_date)
VALUES ('a4f3e1c8-fd4b-4088-9c2d-9d36490e08a7', 'SERIES-2', 1234, true, '2024-07-31', '2025-07-31');

INSERT INTO telemechanic.ewb (id, author_id, medic_id, medic_request_id, medic_decision_time, request_id, creation_time, status, human_readable_id,
                              ewb_uuid, start_date, finish_date, organization_id, transport_id, driver_id, driver_license_id, telemech_out_id,
                              telemech_decision_out, telemech_in_id, telemech_decision_in, attorney_out_id, odometer_out, odometer_in,
                              organization_medical_license_id, tariff_department_id, time_zone, transportation_type, communication_type)
VALUES ('a4f3e1c8-fd4b-4088-9c2d-9d36490e08a7', '95c2bd4f-3a79-4b30-8d7e-9308e751b40a', '95c2bd4f-3a79-4b30-8d7e-9308e751b40a','5456bbff-1f45-4f9a-90d6-85304513d54c',
        '2024-07-31 10:11:39.000000', 'd9d50a1e-1fc4-4458-ade6-f5297304a389', '2023-07-31 10:11:39.000000', 'TELEMECH_IN_PROGRESS', 'EWB_ID4',
        '4781464b-5c77-4ec0-a326-7dce93fcaa1c', '2024-07-31', '2024-07-31',
        'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', '6a897664-9a0f-4e58-b3b7-d666579b37cb',
        '167a0b4c-8324-44ba-9619-c0cd583fb1ca', 'e1f2f2a3-fd1b-4088-9c2d-9d36490e08a7', '95c2bd4f-3a79-4b30-8d7e-9308e751b40a', '2024-07-31 10:11:39.000000', '95c2bd4f-3a79-4b30-8d7e-9308e751b40a',
        '2024-07-31 10:11:39.000000' , 'fc1bc338-ecf3-4113-b862-d9bb9d8779c4',
        null, null, 'd8686e9a-a50c-4c6f-a581-e01ef501e983', '20d4a338-e121-4a0b-9d80-a7b6b035484f', 'UTC+03:00', 'COMMERCIAL_TRANSPORTATION', 'URBAN');

INSERT INTO telemechanic.ewb (id, author_id, medic_id, medic_request_id, medic_decision_time, request_id, creation_time, status, human_readable_id,
                              ewb_uuid, start_date, finish_date, organization_id, transport_id, driver_id, driver_license_id, telemech_out_id,
                              telemech_decision_out, telemech_in_id, telemech_decision_in, attorney_out_id, odometer_out, odometer_in,
                              organization_medical_license_id, tariff_department_id, time_zone, transportation_type, communication_type)
VALUES ('29d4ef98-51fa-43b3-b3ed-a9870ad4b659', '95c2bd4f-3a79-4b30-8d7e-9308e751b40a', '95c2bd4f-3a79-4b30-8d7e-9308e751b40a','8ad5e9c6-604f-4baa-861f-d7d4c320eb72',
        '2024-07-31 10:11:39.000000', 'd9d50a1e-1fc4-4458-ade6-f5297304a389', '2023-07-31 10:11:39.000000', 'TELEMECH_IN_PROGRESS', 'EWB_ID5',
        'a4797f06-a46e-49a3-bf28-6a79b188216f', '2024-07-31', '2024-07-31',
        '621c288d-e348-46e5-a319-cbf61ef1e396', '6a897664-9a0f-4e58-b3b7-d666579b37cb',
        '167a0b4c-8324-44ba-9619-c0cd583fb1ca', 'e1f2f2a3-fd1b-4088-9c2d-9d36490e08a7', '95c2bd4f-3a79-4b30-8d7e-9308e751b40a', '2024-07-31 10:11:39.000000', '95c2bd4f-3a79-4b30-8d7e-9308e751b40a',
        '2024-07-31 10:11:39.000000' , 'fc1bc338-ecf3-4113-b862-d9bb9d8779c4',
        null, null, 'a4f3e1c8-fd4b-4088-9c2d-9d36490e08a7', '20d4a338-e121-4a0b-9d80-a7b6b035484f', 'UTC+03:00', 'OWN_ACCOUNT_TRANSPORTATION', 'URBAN');

INSERT INTO telemechanic.ewb (id, author_id, medic_id, medic_request_id, medic_decision_time, request_id, creation_time, status, human_readable_id,
                              ewb_uuid, start_date, finish_date, organization_id, transport_id, driver_id, driver_license_id, telemech_out_id,
                              telemech_decision_out, telemech_in_id, telemech_decision_in, attorney_out_id, odometer_out, odometer_in,
                              organization_medical_license_id, tariff_department_id, time_zone, transportation_type, communication_type)
VALUES  ('8800a83d-e9a1-4fc9-b85d-be1221fd294e', '95c2bd4f-3a79-4b30-8d7e-9308e751b40a', '95c2bd4f-3a79-4b30-8d7e-9308e751b40a','8ad5e9c6-604f-4baa-861f-d7d4c320eb72',
        '2024-07-31 10:11:39.000000', 'd9d50a1e-1fc4-4458-ade6-f5297304a389', '2023-07-31 10:11:39.000000', 'ON_THE_LINE', 'EWB_ID6',
        'a60c8143-feb4-490d-b459-8ba7e0cd388f', current_date, current_date,
        '621c288d-e348-46e5-a319-cbf61ef1e396', '6a897664-9a0f-4e58-b3b7-d666579b37cb',
        '167a0b4c-8324-44ba-9619-c0cd583fb1ca', 'e1f2f2a3-fd1b-4088-9c2d-9d36490e08a7', '95c2bd4f-3a79-4b30-8d7e-9308e751b40a', '2024-07-31 10:11:39.000000', '95c2bd4f-3a79-4b30-8d7e-9308e751b40a',
        '2024-07-31 10:11:39.000000' , 'fc1bc338-ecf3-4113-b862-d9bb9d8779c4',
        null, null, 'a4f3e1c8-fd4b-4088-9c2d-9d36490e08a7', '20d4a338-e121-4a0b-9d80-a7b6b035484f', 'UTC+03:00', 'COMMERCIAL_TRANSPORTATION', 'URBAN');

INSERT INTO telemechanic.ewb (id, author_id, medic_id, medic_request_id, medic_decision_time, request_id, creation_time, status, human_readable_id,
    ewb_uuid, start_date, finish_date, organization_id, transport_id, driver_id, driver_license_id, telemech_out_id,
    telemech_decision_out, telemech_in_id, telemech_decision_in, attorney_out_id, odometer_out, odometer_in, fuel_litreage_out,
    fuel_litreage_in, tariff_department_id, time_zone, transportation_type, communication_type)
VALUES ('ac938903-e81e-4267-a856-796963f1f41e', '019ff061-c0dd-708a-ac10-787b06956003', null, null, null, '019ff050-1d6f-7488-bb5e-f10941c5cfd5',
    current_date, 'TELEMECH_IN_PROGRESS', 'EWB_ID7', 'f3c776aa-7c28-44a0-9894-0ae7c328da07', current_date, current_date,
    'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', 'b29ac76a-16c4-49cc-ad7c-f1c719c49f00', '019ff069-4754-71f2-ab5e-768a795cbc0e', '019ff0a8-f391-7479-8f9a-b8488b35bec8',
    '95c2bd4f-3a79-4b30-8d7e-9308e751b40a', current_date, null, current_date, 'fc1bc338-ecf3-4113-b862-d9bb9d8779c4',
    10000, null, 30, null, '20d4a338-e121-4a0b-9d80-a7b6b035484f', 'UTC+03:00', 'OWN_ACCOUNT_TRANSPORTATION', 'URBAN');