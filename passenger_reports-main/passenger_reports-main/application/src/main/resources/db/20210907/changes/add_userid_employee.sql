ALTER TABLE reports.employee
  ADD COLUMN user_id UUID;

COMMENT ON COLUMN reports.employee.user_id is 'Пользователь';