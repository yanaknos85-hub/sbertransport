INSERT INTO vehicle.transport_department (transport_id, department_id)
SELECT id, department_id FROM vehicle.transport WHERE department_id IS NOT NULL;

INSERT INTO vehicle.transport_organization (transport_id, organization_id)
SELECT id, organization_id FROM vehicle.transport WHERE organization_id IS NOT NULL;