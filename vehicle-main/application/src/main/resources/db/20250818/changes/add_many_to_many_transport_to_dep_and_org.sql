CREATE TABLE vehicle.transport_department (
    transport_id UUID NOT NULL,
    department_id UUID NOT NULL,
    PRIMARY KEY (transport_id, department_id),
    CONSTRAINT fk_transport_department_transport FOREIGN KEY (transport_id) REFERENCES vehicle.transport(id),
    CONSTRAINT fk_transport_department_department FOREIGN KEY (department_id) REFERENCES vehicle.department(id)
);

COMMENT ON TABLE vehicle.transport_department IS 'Таблица связи между транспортом и департаментом (многие ко многим)';
COMMENT ON COLUMN vehicle.transport_department.transport_id IS 'Идентификатор транспорта';
COMMENT ON COLUMN vehicle.transport_department.department_id IS 'Идентификатор департамента';

CREATE INDEX idx_transport_department_transport_id ON vehicle.transport_department(transport_id);
CREATE INDEX idx_transport_department_department_id ON vehicle.transport_department(department_id);

CREATE TABLE vehicle.transport_organization (
    transport_id UUID NOT NULL,
    organization_id UUID NOT NULL,
    PRIMARY KEY (transport_id, organization_id),
    CONSTRAINT fk_transport_organization_transport FOREIGN KEY (transport_id) REFERENCES vehicle.transport(id),
    CONSTRAINT fk_transport_organization_organization FOREIGN KEY (organization_id) REFERENCES vehicle.organization(id)
);

COMMENT ON TABLE vehicle.transport_organization IS 'Таблица связи между транспортом и организацией (многие ко многим)';
COMMENT ON COLUMN vehicle.transport_organization.transport_id IS 'Идентификатор транспорта';
COMMENT ON COLUMN vehicle.transport_organization.organization_id IS 'Идентификатор организации';

CREATE INDEX idx_transport_organization_transport_id ON vehicle.transport_organization(transport_id);
CREATE INDEX idx_transport_organization_organization_id ON vehicle.transport_organization(organization_id);