-- ORGANIZATION_1
INSERT INTO tariff.organization (id, digit_id, name, active)
VALUES ('cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', 8, 'ЦА', true);
-- DEPARTMENT_1
INSERT INTO tariff.message_department (id,  department_name, human_readable_id, organization_id, parent_id, code)
VALUES ('482e6dcb-03a9-4927-90b4-c7081114a9d8', 'ПАО «Сбербанк России» (ЦА)', 'DT-0008-00000161', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', null, '3');
-- POSITION_1
INSERT INTO tariff.message_position (id, position_name, self_approved, organization_id, active) VALUES
	 ('69a4d3f0-223b-46f5-8fed-0e856a1889dc'::uuid,'Планктон',true,'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'::uuid, true);
-- PERSON_1
INSERT INTO tariff.message_employee (id,first_name,last_name,patronymic,department_id,position_id,user_id) VALUES
	 ('3cd35c19-fd39-413c-99a0-30f35bd642a8'::uuid,'Петр','Петров',null,'482e6dcb-03a9-4927-90b4-c7081114a9d8'::uuid,
	 '69a4d3f0-223b-46f5-8fed-0e856a1889dc'::uuid,'3cd35c19-fd39-413c-99a0-30f35bd642a8'::uuid);
--PERSON_3
INSERT INTO tariff.message_employee (id,first_name,last_name,patronymic,department_id,position_id,user_id) VALUES
	 ('7dd56ea0-fa38-400d-93a6-2a4ef4b7df70'::uuid,'Александр','Александров','Александрович','482e6dcb-03a9-4927-90b4-c7081114a9d8'::uuid,
	 '69a4d3f0-223b-46f5-8fed-0e856a1889dc'::uuid,'7dd56ea0-fa38-400d-93a6-2a4ef4b7df70'::uuid);

-- ORGANIZATION_2
INSERT INTO tariff.organization (id, digit_id, name, active)
VALUES ('621c288d-e348-46e5-a319-cbf61ef1e396', 11, 'Тест2', true);
-- DEPARTMENT_2
INSERT INTO tariff.message_department (id, department_name, human_readable_id, organization_id, parent_id, code)
VALUES ('489A0090-1819-4C60-A611-572EA115C6A4', 'Test2', 'DT-0008-00000222', '621c288d-e348-46e5-a319-cbf61ef1e396', null, '4');
-- POSITION_2
INSERT INTO tariff.message_position (id, position_name, self_approved, organization_id, active) VALUES
	 ('3908803B-A08C-4D96-AA71-9DD5A6D3ABD4'::uuid,'Планктон',true,'621c288d-e348-46e5-a319-cbf61ef1e396'::uuid, true);
-- PERSON_2
INSERT INTO tariff.message_employee (id,first_name,last_name,patronymic,department_id,position_id,user_id) VALUES
	 ('15BDC75D-533B-44BB-9539-3DDC8693F3D3'::uuid,'Иван','Иавнов','Иванович','489A0090-1819-4C60-A611-572EA115C6A4'::uuid,
	 '3908803B-A08C-4D96-AA71-9DD5A6D3ABD4'::uuid,'15BDC75D-533B-44BB-9539-3DDC8693F3D3'::uuid);