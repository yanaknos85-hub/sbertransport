CREATE TABLE telemechanic.transport_organization (
    transport_id UUID NOT NULL,
    organization_id UUID NOT NULL,
    PRIMARY KEY (transport_id, organization_id),
    FOREIGN KEY (transport_id) REFERENCES telemechanic.transport(id),
    FOREIGN KEY (organization_id) REFERENCES telemechanic.organization(id)
);

COMMENT ON TABLE telemechanic.transport_organization IS 'Таблица связи между транспортом и организациями';

COMMENT ON COLUMN telemechanic.transport_organization.transport_id IS 'Идентификатор транспорта';
COMMENT ON COLUMN telemechanic.transport_organization.organization_id IS 'Идентификатор организации';

CREATE INDEX idx_transport_organization_organization_id ON telemechanic.transport_organization (organization_id);

CREATE INDEX idx_transport_organization_transport_id ON telemechanic.transport_organization (transport_id);