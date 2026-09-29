ALTER TABLE request_aggregation.users
    ADD COLUMN employee_id UUID;

COMMENT ON COLUMN request_aggregation.users.employee_id
IS 'Идентификатор сотрудника';