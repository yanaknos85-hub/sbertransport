-- ORGANIZATION_1
INSERT INTO reports.organization (id)
VALUES ('cb9f17e7-f658-43ca-a70f-40c1c93ad0a6');
-- DEPARTMENT_1
INSERT INTO reports.department (id, department_name, organization_id, parent_id, human_readable_id, code)
VALUES ('482e6dcb-03a9-4927-90b4-c7081114a9d8', 'ПАО «Сбербанк России» (ЦА)',
        'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', null, 'DT-0008-1', '2401005');
-- POSITION_1
INSERT INTO reports."position" (id, position_name)
VALUES ('69a4d3f0-223b-46f5-8fed-0e856a1889dc', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6');
-- PERSON_1
INSERT INTO reports.employee (id, human_readable_id, first_name, last_name, patronymic,
                                        personnel_number, user_id, department_id, position_id,
                                        organization_id)
VALUES ('3cd35c19-fd39-413c-99a0-30f35bd642a8', 'US-0008-00044610', 'Петр', 'Петров', null,
        '2016497', '3cd35c19-fd39-413c-99a0-30f35bd642a8', '482e6dcb-03a9-4927-90b4-c7081114a9d8',
        '69a4d3f0-223b-46f5-8fed-0e856a1889dc', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6');
--PERSON_3
INSERT INTO reports.employee (id, human_readable_id, first_name, last_name, patronymic,
                                                                     personnel_number, user_id, department_id, position_id,
                                                                     organization_id)
VALUES ('7dd56ea0-fa38-400d-93a6-2a4ef4b7df70', 'US-0008-00044612', 'Александр', 'Александров', 'Александрович',
        '3016497', '7dd56ea0-fa38-400d-93a6-2a4ef4b7df70', '482e6dcb-03a9-4927-90b4-c7081114a9d8',
        '69a4d3f0-223b-46f5-8fed-0e856a1889dc', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6');

-- ORGANIZATION_2
INSERT INTO reports.organization (id)
VALUES ('621c288d-e348-46e5-a319-cbf61ef1e396');
-- DEPARTMENT_2
INSERT INTO reports.department (id, department_name, organization_id, parent_id, human_readable_id, code)
VALUES ('489A0090-1819-4C60-A611-572EA115C6A4', 'Test2',
        '621c288d-e348-46e5-a319-cbf61ef1e396', null, 'DT-0001-2', '2401006');
-- CHILD_DEPARTMENT_1
INSERT INTO reports.department (id, department_name, organization_id, parent_id, human_readable_id, code)
VALUES ('c3820eb5-e6a5-4fb6-a5c9-1e88a0e5055d', 'Test_Child_1',
        '621c288d-e348-46e5-a319-cbf61ef1e396', '489A0090-1819-4C60-A611-572EA115C6A4', 'DT-0001-7', '1802001');
-- POSITION_2
INSERT INTO reports."position" (id, position_name)
VALUES ('3908803B-A08C-4D96-AA71-9DD5A6D3ABD4', '621c288d-e348-46e5-a319-cbf61ef1e396');
-- PERSON_2
INSERT INTO reports.employee (id, human_readable_id, first_name, last_name, patronymic,
                                                                     personnel_number, user_id, department_id, position_id,
                                                                     organization_id)
VALUES ('15BDC75D-533B-44BB-9539-3DDC8693F3D3', 'US-0008-00044611', 'Иван', 'Иванов', 'Иванович',
        '2016498', '15BDC75D-533B-44BB-9539-3DDC8693F3D3', '489A0090-1819-4C60-A611-572EA115C6A4',
        '3908803B-A08C-4D96-AA71-9DD5A6D3ABD4', '621c288d-e348-46e5-a319-cbf61ef1e396');

-- ORGANIZATION_3
INSERT INTO reports.organization (id)
VALUES ('11501599-b498-4e9c-8b75-3e5899c445c0');
-- DEPARTMENT_3
INSERT INTO reports.department (id, department_name, organization_id, parent_id, human_readable_id, code)
VALUES ('21f90644-fb63-4225-a794-c6062ad53e56', 'Test_dep_3',
        '11501599-b498-4e9c-8b75-3e5899c445c0', null, 'DT-0001-9', '2222');
-- CHILD_DEPARTMENT_3_2
INSERT INTO reports.department (id, department_name, organization_id, parent_id, human_readable_id, code)
VALUES ('3fb86138-542a-4605-822b-8b4729b6eaf6', 'Test_dep_child_3_2',
        '11501599-b498-4e9c-8b75-3e5899c445c0', '21f90644-fb63-4225-a794-c6062ad53e56', 'DT-0001-11', 'ТЕСТ2');
-- POSITION_3
INSERT INTO reports."position" (id, position_name)
VALUES ('d154bbb7-2221-450f-aa9a-deeb1576a477', '11501599-b498-4e9c-8b75-3e5899c445c0');
-- PERSON_3
INSERT INTO reports.employee (id, human_readable_id, first_name, last_name, patronymic,
                                                                     personnel_number, user_id, department_id, position_id,
                                                                     organization_id)
VALUES ('c9e9192f-d2f3-4604-83f8-e863ce1bc192', 'US-0008-00033310', 'Иван', 'Иванов', 'Иванович',
        '2016138', 'c9e9192f-d2f3-4604-83f8-e863ce1bc192', '21f90644-fb63-4225-a794-c6062ad53e56',
        'd154bbb7-2221-450f-aa9a-deeb1576a477', '11501599-b498-4e9c-8b75-3e5899c445c0');