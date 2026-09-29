-- Тестовые подразделения
insert into etrn_cargo.department (id, department_name, parent_id, active, organization_id, humanreadableid)
values
    ('b1519c3d-3445-22fc-0c84-416b4b8e0f17', 'Отдел перевозок', null, true, 'a0408b2c-2334-11eb-9b73-305a3a7d9fe6', 'DP-0001-001'),
    ('c2620d4e-4556-33fd-1d95-527c5c9f1e28', 'Логистика', null, true, 'c2055154-8d02-4e1a-ac57-eadbaca44026', 'DP-0002-001');
