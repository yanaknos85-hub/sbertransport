insert into telemechanic.category
(id, category_code, title)
values
('ac98cc7c-8bb8-488c-b4f1-c24d96da3b69', 'А', 'Мотоциклы'),
('be8abe82-1cbd-4f0e-9b31-2b9db90231dc', 'В', 'Легковые автомобили, небольшие грузовики (до 3,5 тонн)'),
('66872805-6447-4ae9-8336-c81afb24bf09', 'С1', 'Средние грузовики (от 3,5 до 7 тонн)');

insert into telemechanic.driving_license
(id , series, number, issue_date, expiry_date, previous_id, active)
values
('8bc3e3a9-d90a-41be-b9eb-af52a26f674c', 's01', 1, '2020-01-01', '2030-01-01', null, false),
('66c38d4c-5f88-42c2-9ae1-a5ea36324d4e', 's02', 1, '2023-01-01', '2033-01-01', '8bc3e3a9-d90a-41be-b9eb-af52a26f674e', true);

insert into telemechanic.driving_license_category
(driving_license_id, category_id)
values
('8bc3e3a9-d90a-41be-b9eb-af52a26f674c', 'be8abe82-1cbd-4f0e-9b31-2b9db90231dc'),
('8bc3e3a9-d90a-41be-b9eb-af52a26f674c', '66872805-6447-4ae9-8336-c81afb24bf09'),
('66c38d4c-5f88-42c2-9ae1-a5ea36324d4e', 'be8abe82-1cbd-4f0e-9b31-2b9db90231dc'),
('66c38d4c-5f88-42c2-9ae1-a5ea36324d4e', '66872805-6447-4ae9-8336-c81afb24bf09'),
('66c38d4c-5f88-42c2-9ae1-a5ea36324d4e', 'ac98cc7c-8bb8-488c-b4f1-c24d96da3b69');

INSERT INTO telemechanic.organization (id, digit_id, active, official_name, msrn, tin)
VALUES ('cb8f17e7-f658-43ca-a70f-50c1c93ad0a6', 8, true, 'ЦА', '88888889', '888889');

INSERT INTO telemechanic.department (id, active, department_name, human_readable_id, organization_id, parent_id)
VALUES ('483e6dcb-09a9-4927-90b4-c7081114a9d8', true, 'ПАО «Сбербанк России» (ЦА 2)', 'DT-0008-00000162',
        'cb8f17e7-f658-43ca-a70f-50c1c93ad0a6', null);

INSERT INTO telemechanic.position (id, active, organization_id, position_name)
VALUES ('68a4d3f0-223b-46f5-8fed-0e857a1888dc', true, 'cb8f17e7-f658-43ca-a70f-50c1c93ad0a6', 'Планктон 2');


INSERT INTO telemechanic.employee (id, active, human_readable_id, first_name, last_name, patronymic, mobile_phone,
                              personnel_number, user_id, department_id, position_id, organization_id)
VALUES ('3cd45c19-fd39-413c-99a0-30f35bd442a8', true, 'US-0008-00055611', 'Евгений', 'Иванов', null, '+79523123123',
        '2046498', '3cd45c19-fd39-413c-99a0-30f35bd442a8', '483e6dcb-09a9-4927-90b4-c7081114a9d8',
        '68a4d3f0-223b-46f5-8fed-0e857a1888dc', 'cb8f17e7-f658-43ca-a70f-50c1c93ad0a6');

INSERT INTO telemechanic.employee (id, active, human_readable_id, first_name, last_name, patronymic, mobile_phone,
                              personnel_number, user_id, department_id, position_id, organization_id)
VALUES ('3cd45c19-fd39-413c-99a9-30f35bd442a8', true, 'US-0008-00055612', 'Евгений2', 'Иванов2', null, '+79533123123',
        '2046499', '3cd45c19-fd39-413c-99a9-30f35bd442a8', '483e6dcb-09a9-4927-90b4-c7081114a9d8',
        '68a4d3f0-223b-46f5-8fed-0e857a1888dc', 'cb8f17e7-f658-43ca-a70f-50c1c93ad0a6');

insert into telemechanic.driver
(id, employee_id, driving_license_id, tin, snils)
values
('0dd8dd1c-04d0-4c1f-9889-2c846881e2ed', '3cd45c19-fd39-413c-99a0-30f35bd442a8', '66c38d4c-5f88-42c2-9ae1-a5ea36324d4e', '0000001', '186-345-573 03');
