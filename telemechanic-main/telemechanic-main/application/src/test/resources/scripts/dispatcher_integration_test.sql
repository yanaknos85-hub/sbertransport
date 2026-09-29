-- ORGANIZATION_1
INSERT INTO telemechanic.organization (id, digit_id, active, official_name, msrn, tin)
VALUES ('cb8f17e7-f658-43ca-a70f-40c1c93ad0a6', 8, true, 'ЦА', '88888889', '888889');
-- DEPARTMENT_1
INSERT INTO telemechanic.department (id, active, department_name, human_readable_id, organization_id, parent_id)
VALUES ('483e6dcb-03a9-4927-90b4-c7081114a9d8', true, 'ПАО «Сбербанк России» (ЦА 2)', 'DT-0008-00000162',
        'cb8f17e7-f658-43ca-a70f-40c1c93ad0a6', null);
-- POSITION_1
INSERT INTO telemechanic.position (id, active, organization_id, position_name)
VALUES ('68a4d3f0-223b-46f5-8fed-0e856a1889dc', true, 'cb8f17e7-f658-43ca-a70f-40c1c93ad0a6', 'Планктон 2');
-- PERSON_1
INSERT INTO telemechanic.employee (id, active, human_readable_id, first_name, last_name, patronymic, mobile_phone,
                              personnel_number, user_id, department_id, position_id, organization_id)
VALUES ('3cd45c19-fd39-413c-99a0-30f35bd642a8', true, 'US-0008-00044611', 'Петр', 'Иванов', null, '+74123123123',
        '2016498', '3cd45c19-fd39-413c-99a0-30f35bd642a8', '483e6dcb-03a9-4927-90b4-c7081114a9d8',
        '68a4d3f0-223b-46f5-8fed-0e856a1889dc', 'cb8f17e7-f658-43ca-a70f-40c1c93ad0a6');
-- ATTORNEY_1
insert into telemechanic.attorney (id, number, issue_date, expiry_date, creation_system)
values ('fc1bc338-ecf3-4113-b862-d9bb9d8779c5', '76a3d37e-636c-4a66-8782-6199fe002f26', '2020-01-01', '2030-01-01', 'sys');
-- DISPATCHER_1
insert into telemechanic.dispatcher (id, employee_id, organization_id, department_id, attorney_id, active)
values ('e8c386ae-0fba-4a26-a425-0e9073ae71f1', '3cd45c19-fd39-413c-99a0-30f35bd642a8', 'cb8f17e7-f658-43ca-a70f-40c1c93ad0a6',
        '483e6dcb-03a9-4927-90b4-c7081114a9d8', 'fc1bc338-ecf3-4113-b862-d9bb9d8779c5', true);
-- PERSON_2
INSERT INTO telemechanic.employee (id, active, human_readable_id, first_name, last_name, patronymic, mobile_phone,
                              personnel_number, user_id, department_id, position_id, organization_id)
VALUES ('1cd45c19-fd39-413c-99a0-30f35bd642a8', true, 'US-0008-00044611', 'Петр', 'Иванов', null, '+74123123123',
        '2016498', '1cd45c19-fd39-413c-99a0-30f35bd642a8', '483e6dcb-03a9-4927-90b4-c7081114a9d8',
        '68a4d3f0-223b-46f5-8fed-0e856a1889dc', 'cb8f17e7-f658-43ca-a70f-40c1c93ad0a6');
-- PERSON_3
INSERT INTO telemechanic.employee (id, active, human_readable_id, first_name, last_name, patronymic, mobile_phone,
                              personnel_number, user_id, department_id, position_id, organization_id)
VALUES ('2cd45c19-fd39-413c-99a0-30f35bd642a8', true, 'US-0008-00044611', 'Петр', 'Иванов', null, '+74123123123',
        '2016498', '2cd45c19-fd39-413c-99a0-30f35bd642a8', '483e6dcb-03a9-4927-90b4-c7081114a9d8',
        '68a4d3f0-223b-46f5-8fed-0e856a1889dc', 'cb8f17e7-f658-43ca-a70f-40c1c93ad0a6');