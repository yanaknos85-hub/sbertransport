INSERT INTO telemechanic.transport_organization (transport_id, organization_id)
SELECT id AS transport_id, organization_id
FROM telemechanic.transport
WHERE organization_id IS NOT NULL;

ALTER TABLE telemechanic.transport DROP COLUMN organization_id;