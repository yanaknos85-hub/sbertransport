-- Минимальный набор тестовых данных для unit-тестов сервисов
-- Организации
insert into etrn_cargo.organization_group (id, "name", internal)
values ('d0519e5f-5667-44cc-8d9d-99aae2c10f01', 'Тестовые организации', false);

insert into etrn_cargo.organization (id, digit_id, active, official_name, address, organization_group_id)
values ('a0408b2c-2334-11eb-9b73-305a3a7d9fe6', 100001, true, 'ООО «ТестЛогистик»', 'г. Москва', 'd0519e5f-5667-44cc-8d9d-99aae2c10f01');

-- Подразделения
insert into etrn_cargo.department (id, department_name, parent_id, active, organization_id, humanreadableid)
values ('b1519c3d-3445-22fc-0c84-416b4b8e0f17', 'Отдел перевозок', null, true, 'a0408b2c-2334-11eb-9b73-305a3a7d9fe6', 'DP-0001-001');

-- Сотрудники
insert into etrn_cargo.employee (id, first_name, last_name, patronymic, personnel_number, user_id, department_id, humanreadableid, active)
values ('f10bcc5b-51db-4e1c-a747-2a229604f974', 'Иван', 'Петров', 'Иванович', 'PN00001', 'f10bcc5b-51db-4e1c-a747-2a229604f974', 'b1519c3d-3445-22fc-0c84-416b4b8e0f17', 'US-0001-1', true);

-- Одна ЭТрН
insert into etrn_cargo.etrn (id, humanreadableid, status, sender_name, timezone, active, created_at, updated_at, version)
values ('cf8f42df-998a-48b4-b349-1a33f0c1072b', 'ETRN-0001-00000001', 'IDENTIFIED', 'ООО «ТестЛогистик»', 'Europe/Moscow', true, '2025-01-15 10:00:00', '2025-01-15 12:00:00', 1);
