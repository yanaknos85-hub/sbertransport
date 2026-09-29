-- ORGANIZATION_1
INSERT INTO telemechanic.organization (id, digit_id, active, official_name, msrn, tin, contractor_external_id)
VALUES ('cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', 8, true, 'ЦА', '88888888', '888888', '50683ed0-4afc-4a13-b5c7-5064220b9517');
-- DEPARTMENT_1
INSERT INTO telemechanic.department (id, active, department_name, human_readable_id, organization_id, parent_id, autopark_id, autopark_name)
VALUES ('482e6dcb-03a9-4927-90b4-c7081114a9d8', true, 'ПАО «Сбербанк России» (ЦА)', 'DT-0008-00000161',
        'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', null, '3d30f41d-2186-4d04-b695-ffb6e6be7b87', 'Fortest');
-- POSITION_1
INSERT INTO telemechanic.position (id, active, organization_id, position_name)
VALUES ('69a4d3f0-223b-46f5-8fed-0e856a1889dc', true, 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', 'Планктон');
-- PERSON_1
INSERT INTO telemechanic.employee (id, active, human_readable_id, first_name, last_name, patronymic, mobile_phone,
                              personnel_number, user_id, department_id, position_id, organization_id)
VALUES ('3cd35c19-fd39-413c-99a0-30f35bd642a8', true, 'US-0008-00044610', 'Петр', 'Петров', null, '+73123123123',
        '2016497', '3cd35c19-fd39-413c-99a0-30f35bd642a8', '482e6dcb-03a9-4927-90b4-c7081114a9d8',
        '69a4d3f0-223b-46f5-8fed-0e856a1889dc', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6');
--PERSON_3
INSERT INTO telemechanic.employee (id, active, human_readable_id, first_name, last_name, patronymic, mobile_phone,
                              personnel_number, user_id, department_id, position_id, organization_id)
VALUES ('7dd56ea0-fa38-400d-93a6-2a4ef4b7df70', true, 'US-0008-00044612', 'Александр', 'Александров', 'Александрович',
        '+73123123123', '3016497', '7dd56ea0-fa38-400d-93a6-2a4ef4b7df70', '482e6dcb-03a9-4927-90b4-c7081114a9d8',
        '69a4d3f0-223b-46f5-8fed-0e856a1889dc', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6');
INSERT INTO telemechanic.tin (id, employee_id, tin)
VALUES('6f787ce7-319f-4d90-9642-217027eb829f', '7dd56ea0-fa38-400d-93a6-2a4ef4b7df70', '123456488880');
insert into telemechanic.contact (id, type, value)
VALUES ('bdc1789e-7b12-47b5-8553-077406f0ad3c', 'PHONE', '+79163313365'),
       ('d05f9447-c9ce-47fa-8a54-655cea9ba324', 'EMAIL', '4343@mail.ru');
insert into telemechanic.organization_contact (organization_id, contact_id)
VALUES ('cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', 'bdc1789e-7b12-47b5-8553-077406f0ad3c'),
       ('cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', 'd05f9447-c9ce-47fa-8a54-655cea9ba324');

-- ORGANIZATION_2
INSERT INTO telemechanic.organization (id, digit_id, active, official_name, msrn, tin)
VALUES ('621c288d-e348-46e5-a319-cbf61ef1e396', 11, true, 'Тест2', '11111111', '111111');
-- DEPARTMENT_2
INSERT INTO telemechanic.department (id, active, department_name, human_readable_id, organization_id, parent_id)
VALUES ('489A0090-1819-4C60-A611-572EA115C6A4', true, 'Test2', 'DT-0008-00000222',
        '621c288d-e348-46e5-a319-cbf61ef1e396', null);
-- CHILD_DEPARTMENT_1
INSERT INTO telemechanic.department (id, active, department_name, human_readable_id, organization_id, parent_id)
VALUES ('c3820eb5-e6a5-4fb6-a5c9-1e88a0e5055d', true, 'Test_Child_1', 'DT-0008-00000223',
        '621c288d-e348-46e5-a319-cbf61ef1e396', '489A0090-1819-4C60-A611-572EA115C6A4');
-- POSITION_2
INSERT INTO telemechanic.position (id, active, organization_id, position_name)
VALUES ('3908803B-A08C-4D96-AA71-9DD5A6D3ABD4', true, '621c288d-e348-46e5-a319-cbf61ef1e396', 'Планктон');
-- PERSON_2
INSERT INTO telemechanic.employee (id, active, human_readable_id, first_name, last_name, patronymic, mobile_phone,
                              personnel_number, user_id, department_id, position_id, organization_id)
VALUES ('15BDC75D-533B-44BB-9539-3DDC8693F3D3', true, 'US-0008-00044610', 'Иван', 'Иванов', 'Иванович', '+73123123124',
        '2016498', '15BDC75D-533B-44BB-9539-3DDC8693F3D3', '489A0090-1819-4C60-A611-572EA115C6A4',
        '3908803B-A08C-4D96-AA71-9DD5A6D3ABD4', '621c288d-e348-46e5-a319-cbf61ef1e396');

-- ORGANIZATION_3
INSERT INTO telemechanic.organization (id, digit_id, active, official_name, msrn, tin)
VALUES ('11501599-b498-4e9c-8b75-3e5899c445c0', 77, true, 'Тест3', '11111122', '111122');
-- DEPARTMENT_3
INSERT INTO telemechanic.department (id, active, department_name, human_readable_id, organization_id, parent_id)
VALUES ('21f90644-fb63-4225-a794-c6062ad53e56', true, 'Test_dep_3', 'DT-0008-00000333',
        '11501599-b498-4e9c-8b75-3e5899c445c0', null);
-- CHILD_DEPARTMENT_3_2
INSERT INTO telemechanic.department (id, active, department_name, human_readable_id, organization_id, parent_id)
VALUES ('e4cdf1e8-45d6-4ecf-8330-6b10760d256d', true, 'Test_dep_child_3_2', 'DT-0008-00003332',
        '11501599-b498-4e9c-8b75-3e5899c445c0', '21f90644-fb63-4225-a794-c6062ad53e56');
-- POSITION_3
INSERT INTO telemechanic.position (id, active, organization_id, position_name)
VALUES ('d154bbb7-2221-450f-aa9a-deeb1576a477', true, '11501599-b498-4e9c-8b75-3e5899c445c0', 'Планктон');
-- PERSON_3
INSERT INTO telemechanic.employee (id, active, human_readable_id, first_name, last_name, patronymic, mobile_phone,
                              personnel_number, user_id, department_id, position_id, organization_id)
VALUES ('c9e9192f-d2f3-4604-83f8-e863ce1bc192', true, 'US-0008-00033310', 'Иван', 'Иванов', 'Иванович', '+73123123124',
        '2016138', 'c9e9192f-d2f3-4604-83f8-e863ce1bc192', '21f90644-fb63-4225-a794-c6062ad53e56',
        'd154bbb7-2221-450f-aa9a-deeb1576a477', '11501599-b498-4e9c-8b75-3e5899c445c0');
-- PERSON_4
INSERT INTO telemechanic.employee (id, active, human_readable_id, first_name, last_name, patronymic, mobile_phone,
    personnel_number, user_id, department_id, position_id, organization_id)
VALUES ('819bd262-ac52-4b00-8206-e6c060b314e5', false, 'US-0008-33002', 'Иван', 'Иванов', 'Иванович', '+79203127392',
    '34534288', '819bd262-ac52-4b00-8206-e6c060b314e5', '21f90644-fb63-4225-a794-c6062ad53e56',
    'd154bbb7-2221-450f-aa9a-deeb1576a477', '11501599-b498-4e9c-8b75-3e5899c445c0');