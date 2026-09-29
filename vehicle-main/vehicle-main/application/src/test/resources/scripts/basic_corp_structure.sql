-- ORGANIZATION_1
INSERT INTO vehicle.organization (id, digit_id, active, official_name)
VALUES ('cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', 8, true, 'ЦА');
-- DEPARTMENT_1
INSERT INTO vehicle.department (id, active, department_name, human_readable_id, organization_id, parent_id)
VALUES ('482e6dcb-03a9-4927-90b4-c7081114a9d8', true, 'ПАО «Сбербанк России» (ЦА)', 'DT-0008-00000161',
        'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', null);
-- POSITION_1
INSERT INTO vehicle.position (id, active, organization_id, position_name)
VALUES ('69a4d3f0-223b-46f5-8fed-0e856a1889dc', true, 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', 'Планктон');
-- PERSON_1
INSERT INTO vehicle.employee (id, active, human_readable_id, first_name, last_name, patronymic, mobile_phone,
                              personnel_number, user_id, department_id, position_id, organization_id)
VALUES ('3cd35c19-fd39-413c-99a0-30f35bd642a8', true, 'US-0008-00044610', 'Петров', 'Петр', null, '+73123123123',
        '2016497', '3cd35c19-fd39-413c-99a0-30f35bd642a8', '482e6dcb-03a9-4927-90b4-c7081114a9d8',
        '69a4d3f0-223b-46f5-8fed-0e856a1889dc', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6');
--PERSON_3
INSERT INTO vehicle.employee (id, active, human_readable_id, first_name, last_name, patronymic, mobile_phone,
                              personnel_number, user_id, department_id, position_id, organization_id)
VALUES ('7dd56ea0-fa38-400d-93a6-2a4ef4b7df70', true, 'US-0008-00044612', 'Александров', 'Александр', 'Александрович',
        '+73123123123', '3016497', '7dd56ea0-fa38-400d-93a6-2a4ef4b7df70', '482e6dcb-03a9-4927-90b4-c7081114a9d8',
        '69a4d3f0-223b-46f5-8fed-0e856a1889dc', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6');


-- ORGANIZATION_2
INSERT INTO vehicle.organization (id, digit_id, active, official_name)
VALUES ('621c288d-e348-46e5-a319-cbf61ef1e396', 11, true, 'Тест2');
-- DEPARTMENT_2
INSERT INTO vehicle.department (id, active, department_name, human_readable_id, organization_id, parent_id)
VALUES ('489A0090-1819-4C60-A611-572EA115C6A4', true, 'Test2', 'DT-0008-00000222',
        '621c288d-e348-46e5-a319-cbf61ef1e396', null);
-- POSITION_2
INSERT INTO vehicle.position (id, active, organization_id, position_name)
VALUES ('3908803B-A08C-4D96-AA71-9DD5A6D3ABD4', true, '621c288d-e348-46e5-a319-cbf61ef1e396', 'Планктон');
-- PERSON_2
INSERT INTO vehicle.employee (id, active, human_readable_id, first_name, last_name, patronymic, mobile_phone,
                              personnel_number, user_id, department_id, position_id, organization_id)
VALUES ('15BDC75D-533B-44BB-9539-3DDC8693F3D3', true, 'US-0008-00044610', 'Иван', 'Иавнов', 'Иавнович', '+73123123124',
        '2016498', '15BDC75D-533B-44BB-9539-3DDC8693F3D3', '489A0090-1819-4C60-A611-572EA115C6A4',
        '3908803B-A08C-4D96-AA71-9DD5A6D3ABD4', '621c288d-e348-46e5-a319-cbf61ef1e396');