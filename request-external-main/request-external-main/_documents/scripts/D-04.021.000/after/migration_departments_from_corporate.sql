-- update department
UPDATE external_request.department erd
SET organization_id = cd.organization_id, status = cd.status::text::external_request.department_status
    FROM corporate.department cd
WHERE erd.id = cd.id
