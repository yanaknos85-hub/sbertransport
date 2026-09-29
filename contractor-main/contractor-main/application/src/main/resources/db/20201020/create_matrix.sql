CREATE TABLE IF NOT EXISTS contractors.methods
(
    id          UUID PRIMARY KEY,
    name        VARCHAR NOT NULL UNIQUE,
    description VARCHAR
);

COMMENT ON TABLE contractors.methods IS 'Информация о методах';
COMMENT ON COLUMN contractors.methods.id IS 'ID метода';
COMMENT ON COLUMN contractors.methods.name IS 'Наименование метода';
COMMENT ON COLUMN contractors.methods.description IS 'Описание метода';

CREATE TABLE IF NOT EXISTS contractors.method_roles
(
    id        UUID PRIMARY KEY,
    method_id UUID REFERENCES contractors.methods (id),
    role_code VARCHAR,
    CONSTRAINT method_roles_role_method_uk UNIQUE (method_id, role_code)
);

COMMENT ON TABLE contractors.method_roles IS 'Связи методов и ролей';
COMMENT ON COLUMN contractors.method_roles.id IS 'ID связи';
COMMENT ON COLUMN contractors.method_roles.method_id IS 'ID метода';
COMMENT ON COLUMN contractors.method_roles.role_code IS 'Наименование роли';





