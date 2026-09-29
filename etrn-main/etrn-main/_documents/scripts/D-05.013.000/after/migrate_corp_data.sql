TRUNCATE TABLE
etrn_cargo.department,
etrn_cargo.employee,
etrn_cargo.organization,
etrn_cargo.organization_group
RESTART IDENTITY CASCADE;

-- ============================================================================
-- 1. Справочные таблицы (без фильтрации — переносим всё)
-- ============================================================================

INSERT INTO etrn_cargo.organization_group
(id, "name", internal)
SELECT
    og.id, og."name", og.internal
FROM corporate.organization_group og
ON CONFLICT (id) DO NOTHING;

INSERT INTO etrn_cargo.organization (
select id, digit_id, true as active, official_name, address, organization_group_id
from corporate.organization co
where status='ACTIVE'
    and not exists (select 1 from etrn_cargo.organization cco where cco.id = co.id));

INSERT INTO etrn_cargo.department (
select id, name as department_name, parent_id, true as active, organization_id, humanreadableid
from corporate.department d where  status='ACTIVE'
    and not exists (select 1 from etrn_cargo.department dep where dep.id = d.id));

insert into etrn_cargo.employee (
select id, first_name, last_name, patronymic, personnel_number, user_id, department_id,
       humanreadableid, true as active, mobile_phone, cost_center
from corporate.employee ce where status='ACTIVE'
    and not exists (
                    select 1 from etrn_cargo.employee where ce.id = id));