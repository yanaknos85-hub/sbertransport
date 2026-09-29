-- Тестовые организации
insert into etrn_cargo.organization_group (id, "name", internal)
values ('d0519e5f-5667-44cc-8d9d-99aae2c10f01', 'Тестовые организации', false);

insert into etrn_cargo.organization (id, digit_id, active, official_name, address, organization_group_id)
values
    ('a0408b2c-2334-11eb-9b73-305a3a7d9fe6', 100001, true, 'ООО «ТестЛогистик»', 'г. Москва, ул. Тестовая, д. 1', 'd0519e5f-5667-44cc-8d9d-99aae2c10f01'),
    ('c2055154-8d02-4e1a-ac57-eadbaca44026', 100002, true, 'ООО «ТранспортСервис»', 'г. Санкт-Петербург, ул. Промышленная, д. 10', 'd0519e5f-5667-44cc-8d9d-99aae2c10f01');
