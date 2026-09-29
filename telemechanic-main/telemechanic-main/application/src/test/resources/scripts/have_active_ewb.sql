INSERT INTO telemechanic.position (id, active, organization_id, position_name)
VALUES ('257d43ac-dc52-4dee-9346-580ba442d677', true, 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', 'Не_ожидает_обновления');

INSERT INTO telemechanic.department (id, active, department_name, human_readable_id, easup_id, organization_id,
                                     parent_id)
VALUES ('20d4a338-e121-4a0b-9d80-a7b6b035484f', true, 'Группа разработки', 'DT-0008-1468', '10288394',
        'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', null),
       ('d640b449-4482-4a48-bc05-2bbcfa66b465', true, 'Группа разработки', 'DT-0008-764', '10289556',
        'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', null);

INSERT INTO telemechanic.employee (id, active, human_readable_id, first_name, last_name, patronymic, mobile_phone,
                                   personnel_number, user_id, department_id, position_id, organization_id,
                                   full_name_index)
VALUES  ('40b80a64-a191-46c6-bbbb-16a60e772f7f', true, 'US-0008-00026594', 'Василий', 'Клюнков', 'Александрович', null,
        '1806769', '40b80a64-a191-46c6-bbbb-16a60e772f7f', '20d4a338-e121-4a0b-9d80-a7b6b035484f',
        '257d43ac-dc52-4dee-9346-580ba442d677', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', 'КлюнковВасилийАлександрович'),
        ('95c2bd4f-3a79-4b30-8d7e-9308e751b40a', true, 'US-0008-39344', 'Максим', 'Макаров', 'Дмитриевич', null,
        '1913586', '95c2bd4f-3a79-4b30-8d7e-9308e751b40a', 'd640b449-4482-4a48-bc05-2bbcfa66b465',
        '257d43ac-dc52-4dee-9346-580ba442d677', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', 'МакаровМаксимДмитриевич');

INSERT INTO telemechanic.transport (id, state_number, brand, model, status, type, subtype)
VALUES ('6a897664-9a0f-4e58-b3b7-d666579b37cb', 'А789АА777', 'Classic', 'Lada', 'IN_USE', '-', '-');

INSERT INTO telemechanic.driving_license
(id , series, number, issue_date, expiry_date, previous_id, active)
VALUES ('66c38d4c-5f88-42c2-9ae1-a5ea36324d4e', 's02', 1, '2023-01-01', '2033-01-01', '8bc3e3a9-d90a-41be-b9eb-af52a26f674e', true);

INSERT INTO telemechanic.driver (id, employee_id, driving_license_id, tin, snils)
VALUES ('167a0b4c-8324-44ba-9619-c0cd583fb1ca', '40b80a64-a191-46c6-bbbb-16a60e772f7f', '66c38d4c-5f88-42c2-9ae1-a5ea36324d4e', '0000003', '186-345-573 03');

INSERT INTO telemechanic.ewb (id, author_id, creation_time, status, human_readable_id,
                              ewb_uuid, start_date, finish_date, organization_id, transport_id, driver_id,
                              driver_license_id, tariff_department_id, time_zone)
VALUES  ('09ed5f73-2316-408b-bd44-aa7d375b9734', '95c2bd4f-3a79-4b30-8d7e-9308e751b40a',  '2023-07-31 10:11:39.000000', 'EWB_CREATED', 'EWB_ID7',
        '3a1fc891-de99-4ac9-a33a-32156f1a8a07', current_date + interval '1 day', current_date + interval '2 day',
        '621c288d-e348-46e5-a319-cbf61ef1e396', '6a897664-9a0f-4e58-b3b7-d666579b37cb',
        '167a0b4c-8324-44ba-9619-c0cd583fb1ca', 'e1f2f2a3-fd1b-4088-9c2d-9d36490e08a7', '20d4a338-e121-4a0b-9d80-a7b6b035484f', 'UTC+03:00');