CREATE TABLE IF NOT EXISTS tariff.methods
(
    id          UUID PRIMARY KEY,
    name        VARCHAR NOT NULL UNIQUE,
    description VARCHAR
);

COMMENT ON TABLE tariff.methods IS 'Информация о методах';
COMMENT ON COLUMN tariff.methods.id IS 'ID метода';
COMMENT ON COLUMN tariff.methods.name IS 'Наименование метода';
COMMENT ON COLUMN tariff.methods.description IS 'Описание метода';

CREATE TABLE IF NOT EXISTS tariff.method_roles
(
    id        UUID PRIMARY KEY,
    method_id UUID REFERENCES tariff.methods (id),
    role_code VARCHAR,
    CONSTRAINT method_roles_role_method_uk UNIQUE (method_id, role_code)
);

COMMENT ON TABLE tariff.method_roles IS 'Связи методов и ролей';
COMMENT ON COLUMN tariff.method_roles.id IS 'ID связи';
COMMENT ON COLUMN tariff.method_roles.method_id IS 'ID метода';
COMMENT ON COLUMN tariff.method_roles.role_code IS 'Наименование роли';





