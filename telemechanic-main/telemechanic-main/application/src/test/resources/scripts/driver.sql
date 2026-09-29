insert into telemechanic.category
(id, category_code, title)
values
('ac98cc7c-8bb8-488c-b4f1-c24d96da3b62', 'А', 'Мотоциклы'),
('be8abe82-1cbd-4f0e-9b31-2b9db90231df', 'В', 'Легковые автомобили, небольшие грузовики (до 3,5 тонн)'),
('66872805-6447-4ae9-8336-c81afb24bf06', 'С1', 'Средние грузовики (от 3,5 до 7 тонн)');

insert into telemechanic.driving_license
(id , series, number, issue_date, expiry_date, previous_id, active)
values
('8bc3e3a9-d90a-41be-b9eb-af52a26f674e', '0123', '001122', '2020-01-01', '2030-01-01', null, false),
('66c38d4c-5f88-42c2-9ae1-a5ea36324d4e', 's02', 1, '2023-01-01', '2033-01-01', '8bc3e3a9-d90a-41be-b9eb-af52a26f674e', true),
('f5c04d3f-e66e-4f35-9cd5-e70fc9cbed41', 's03', 1, '2000-01-01', '2000-01-02', null, true),
('97155771-fe7d-4df5-a275-c4f2b56387e2', 's04', 1, '2000-01-01', '2026-01-02', null, false);

insert into telemechanic.driving_license_category
(driving_license_id, category_id)
values
('8bc3e3a9-d90a-41be-b9eb-af52a26f674e', 'be8abe82-1cbd-4f0e-9b31-2b9db90231df'),
('8bc3e3a9-d90a-41be-b9eb-af52a26f674e', '66872805-6447-4ae9-8336-c81afb24bf06'),
('66c38d4c-5f88-42c2-9ae1-a5ea36324d4e', 'be8abe82-1cbd-4f0e-9b31-2b9db90231df'),
('66c38d4c-5f88-42c2-9ae1-a5ea36324d4e', '66872805-6447-4ae9-8336-c81afb24bf06'),
('66c38d4c-5f88-42c2-9ae1-a5ea36324d4e', 'ac98cc7c-8bb8-488c-b4f1-c24d96da3b62');

insert into telemechanic.driver
(id, employee_id, driving_license_id, tin, snils, contractor_id, autopark_id)
values
('0dd8dd1c-04d0-4c1f-9889-2c846881e2ed', '15BDC75D-533B-44BB-9539-3DDC8693F3D3', '66c38d4c-5f88-42c2-9ae1-a5ea36324d4e', '0000001',
'186-345-573 03', null, null),
('bc2f5dbf-3585-4b8f-9f37-60ba133e5670', '7dd56ea0-fa38-400d-93a6-2a4ef4b7df70', '8bc3e3a9-d90a-41be-b9eb-af52a26f674e', '0000002',
'559-144-305 02', '50683ed0-4afc-4a13-b5c7-5064220b9517', '3d30f41d-2186-4d04-b695-ffb6e6be7b87'),
('5c2111d7-40d7-405c-bc73-2980c5b633ed', '3cd35c19-fd39-413c-99a0-30f35bd642a8', 'f5c04d3f-e66e-4f35-9cd5-e70fc9cbed41', '0000003',
'112-233-445 01', null, null),
('0cb0b2b0-592d-4ca2-b264-6e6d54168493', '819bd262-ac52-4b00-8206-e6c060b314e5', '97155771-fe7d-4df5-a275-c4f2b56387e2', '0000004',
'763-285-635 04', null, null);