CREATE TABLE IF NOT EXISTS tariff.contract
(
    id              UUID PRIMARY KEY,
    contractor_id   UUID NOT NULL,
    organization_id UUID NOT NULL,
    region          VARCHAR NOT NULL,
    transport_type  VARCHAR NOT NULL,
    service_type    VARCHAR NOT NULL,
    sum             int8,
    user_id         UUID NOT NULL,
    creation_time   timestamp    NOT NULL,
    start_date      date NOT NULL,
    end_date        date NOT NULL,
    active          boolean NOT NULL DEFAULT true
);

COMMENT ON TABLE tariff.contract IS 'Договор с контрагентом';
COMMENT ON COLUMN tariff.contract.id IS 'ID договора';
COMMENT ON COLUMN tariff.contract.contractor_id IS 'Контрагент';
COMMENT ON COLUMN tariff.contract.organization_id IS 'Организация';
COMMENT ON COLUMN tariff.contract.region IS 'Регион';
COMMENT ON COLUMN tariff.contract.transport_type IS 'Тип транспорта';
COMMENT ON COLUMN tariff.contract.service_type IS 'Тип сервиса';
COMMENT ON COLUMN tariff.contract.sum IS 'Сумма';
COMMENT ON COLUMN tariff.contract.user_id IS 'Пользователь создатель записи';
COMMENT ON COLUMN tariff.contract.creation_time IS 'Время создания записи';
COMMENT ON COLUMN tariff.contract.start_date IS 'Дата начала договора';
COMMENT ON COLUMN tariff.contract.end_date IS 'Дата окончания договора';
COMMENT ON COLUMN tariff.contract.active IS 'Флаг активности';






