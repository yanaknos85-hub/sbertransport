CREATE TABLE vehicle.transport
(
    id                           UUID           PRIMARY KEY NOT NULL,
    inventory_number             VARCHAR(50)    NOT NULL,
    asset_number                 VARCHAR(50),
    organization                 UUID           NOT NULL,
    department                   UUID           NOT NULL,
    status_id                    UUID           NOT NULL CONSTRAINT vehicle_transport_status_id_fk REFERENCES vehicle.status,
    vehicle_id                   UUID           NOT NULL CONSTRAINT vehicle_transport_vehicle_id_fk REFERENCES vehicle.vehicle,
    subtype_id                   UUID           NOT NULL CONSTRAINT vehicle_transport_subtype_id_fk REFERENCES vehicle.subtype,
    state_number                 VARCHAR(9)     NOT NULL,
    year                         INTEGER        NOT NULL,
    vin_code                     VARCHAR(17)    NOT NULL,
    chassis_number               VARCHAR(17),
    body_number                  VARCHAR(17),
    certificate_number           VARCHAR(15)    NOT NULL,
    passport_number              VARCHAR(15)    NOT NULL,
    body_color                   VARCHAR(50)    NOT NULL,
    navigation                   VARCHAR(50),
    start_year                   INT            NOT NULL,
    end_year                     INT,
    current_mileage              VARCHAR(50)
);

comment on table vehicle.transport is 'Таблица транспортных средств';
comment on column vehicle.transport.id is 'ID ТС';
comment on column vehicle.transport.inventory_number is 'Инвентарный номер';
comment on column vehicle.transport.asset_number is 'Номер основного средства';
comment on column vehicle.transport.organization is 'Организация';
comment on column vehicle.transport.department is 'Подразделение';
comment on column vehicle.transport.status_id is 'Статус';
comment on column vehicle.transport.vehicle_id is 'ID справочник Автомобиль';
comment on column vehicle.transport.subtype_id is 'ID подвид';
comment on column vehicle.transport.state_number is 'Государственный номер ТС';
comment on column vehicle.transport.year is 'Год выпуска';
comment on column vehicle.transport.vin_code is 'VIN-номер';
comment on column vehicle.transport.chassis_number is '№ Шасси';
comment on column vehicle.transport.body_number is '№ кузова';
comment on column vehicle.transport.certificate_number is 'Свидетельство о регистрации (СТС)';
comment on column vehicle.transport.passport_number is 'Номер ПТС';
comment on column vehicle.transport.body_color is 'Цвет кузова';
comment on column vehicle.transport.navigation is 'Навигационная система';
comment on column vehicle.transport.start_year is 'Дата начала эксплуатации';
comment on column vehicle.transport.end_year is 'Дата окончания эксплуатации';
comment on column vehicle.transport.current_mileage is 'Текущий пробег';