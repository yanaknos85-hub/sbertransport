ALTER TABLE request_aggregation.users
    ADD COLUMN human_readable_id VARCHAR,
ADD COLUMN personnel_number VARCHAR,
ADD COLUMN department_id UUID,
ADD COLUMN organization_id UUID;

COMMENT ON COLUMN request_aggregation.users.human_readable_id
IS 'Человекочитаемый идентификатор';

COMMENT ON COLUMN request_aggregation.users.personnel_number
IS 'Табельный номер сотрудника';

COMMENT ON COLUMN request_aggregation.users.department_id
IS 'Идентификатор подразделения';

COMMENT ON COLUMN request_aggregation.users.organization_id
IS 'Идентификатор организации';