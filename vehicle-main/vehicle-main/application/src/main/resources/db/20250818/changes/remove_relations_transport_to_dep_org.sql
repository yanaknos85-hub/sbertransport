ALTER TABLE vehicle.transport DROP CONSTRAINT transport_department_id_fk;
ALTER TABLE vehicle.transport DROP CONSTRAINT transport_organization_id_fk;
ALTER TABLE vehicle.transport DROP COLUMN IF EXISTS department_id;
ALTER TABLE vehicle.transport DROP COLUMN IF EXISTS organization_id;