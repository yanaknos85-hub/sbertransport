alter table vehicle.employee
    add column full_name_index varchar(255);

COMMENT ON COLUMN vehicle.employee.full_name_index IS 'Индекс для поиска по ФИО';

update vehicle.employee set full_name_index = (employee.last_name || employee.first_name ||  COALESCE(employee.patronymic, ''));

CREATE OR REPLACE FUNCTION vehicle.set_full_name()
    RETURNS TRIGGER AS $set_full_name$
BEGIN
    NEW.full_name_index = NEW.last_name ||  NEW.first_name || COALESCE(NEW.patronymic, '');
RETURN NEW;
END;
$set_full_name$ language 'plpgsql' SECURITY DEFINER;

CREATE TRIGGER set_full_name_trigger
    BEFORE INSERT OR UPDATE ON vehicle.employee
                         FOR EACH ROW
                         EXECUTE PROCEDURE vehicle.set_full_name();

CREATE INDEX employee_full_name_index_idx ON vehicle.employee (full_name_index);

create index employee_last_name_idx
    on vehicle.employee (last_name);