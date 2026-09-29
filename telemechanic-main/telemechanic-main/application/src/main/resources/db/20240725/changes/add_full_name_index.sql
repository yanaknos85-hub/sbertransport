alter table telemechanic.employee
    add column full_name_index varchar(255);

COMMENT ON COLUMN telemechanic.employee.full_name_index IS 'Индекс для поиска по ФИО';

update telemechanic.employee set full_name_index = (employee.last_name || employee.first_name || employee.patronymic);

CREATE OR REPLACE FUNCTION telemechanic.set_full_name()
    RETURNS TRIGGER AS $set_full_name$
BEGIN
    NEW.full_name_index = NEW.last_name ||  NEW.first_name || COALESCE(NEW.patronymic, '');
    RETURN NEW;
END;
$set_full_name$ language 'plpgsql' SECURITY DEFINER;

CREATE TRIGGER set_full_name_trigger
    BEFORE INSERT OR UPDATE ON telemechanic.employee
    FOR EACH ROW
EXECUTE PROCEDURE telemechanic.set_full_name();

CREATE INDEX employee_full_name_index_idx ON telemechanic.employee (full_name_index);