CREATE TABLE IF NOT EXISTS vehicle.fuel_type_name
(
    fuel_type_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (fuel_type_id, name),
    CONSTRAINT fk_fuel_type_name_fuel_type FOREIGN KEY (fuel_type_id) REFERENCES vehicle.fuel_type(id) ON DELETE CASCADE
);

COMMENT ON TABLE vehicle.fuel_type_name IS 'Справочник возможных вариантов наименования видов топлива';
COMMENT ON COLUMN vehicle.fuel_type_name.fuel_type_id IS 'Идентификатор вида топлива';
COMMENT ON COLUMN vehicle.fuel_type_name.name IS 'Вариант наименования';

CREATE UNIQUE INDEX IF NOT EXISTS fuel_type_name_upper_idx ON vehicle.fuel_type_name (UPPER(name));

COMMENT ON INDEX vehicle.fuel_type_name_upper_idx IS 'Индекс по полю name в верхнем регистре для ускорения поиска без учета регистра';