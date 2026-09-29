alter table tariff.message_employee add column if not exists organization_id uuid;

comment on column tariff.message_employee.organization_id is 'ID организации';