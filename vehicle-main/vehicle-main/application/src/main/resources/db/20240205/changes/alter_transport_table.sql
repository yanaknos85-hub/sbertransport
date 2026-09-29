drop table vehicle.transport;

CREATE TABLE vehicle.transport
(
    id                           UUID           PRIMARY KEY NOT NULL,
    inventory_number             VARCHAR(50),
    asset_number                 VARCHAR(50)    NOT NULL,
    organization_id              UUID           NOT NULL,
    department_id                UUID           NOT NULL,
    status_id                    UUID           NOT NULL CONSTRAINT vehicle_transport_status_id_fk REFERENCES vehicle.status,
    vehicle_id                   UUID           NOT NULL CONSTRAINT vehicle_transport_vehicle_id_fk REFERENCES vehicle.vehicle,
    subtype_id                   UUID           NOT NULL CONSTRAINT vehicle_transport_subtype_id_fk REFERENCES vehicle.subtype,
    state_number                 VARCHAR(9)     NOT NULL,
    year                         VARCHAR(4)     NOT NULL,
    vin_code                     VARCHAR(17)    NOT NULL,
    chassis_number               VARCHAR(17),
    body_number                  VARCHAR(17),
    certificate_number           VARCHAR(15)    NOT NULL,
    certificate_issued_date      DATE           NOT NULL,
    passport_number              VARCHAR(15)    NOT NULL,
    passport_issued_date         DATE           NOT NULL,
    brand_by_passport            VARCHAR(150)   NOT NULL,
    model_by_passport            VARCHAR(150)   NOT NULL,
    body_color                   VARCHAR(50)    NOT NULL,
    telematics_id                UUID           NULL CONSTRAINT vehicle_transport_telematics_id_fk REFERENCES vehicle.telematics,
    exploitation_start           DATE           NOT NULL,
    exploitation_end             DATE,
    current_mileage              INTEGER        NOT NULL
);

comment on table vehicle.transport is 'Таблица транспортных средств';
comment on column vehicle.transport.id is 'ID ТС';
comment on column vehicle.transport.inventory_number is 'Инвентарный номер';
comment on column vehicle.transport.asset_number is 'Номер основного средства';
comment on column vehicle.transport.organization_id is 'Организация';
comment on column vehicle.transport.department_id is 'Подразделение';
comment on column vehicle.transport.status_id is 'Статус';
comment on column vehicle.transport.vehicle_id is 'ID справочник Автомобиль';
comment on column vehicle.transport.subtype_id is 'ID подвид';
comment on column vehicle.transport.state_number is 'Государственный номер ТС';
comment on column vehicle.transport.year is 'Год выпуска';
comment on column vehicle.transport.vin_code is 'VIN-номер';
comment on column vehicle.transport.chassis_number is '№ Шасси';
comment on column vehicle.transport.body_number is '№ кузова';
comment on column vehicle.transport.certificate_number is 'Свидетельство о регистрации (СТС)';
comment on column vehicle.transport.certificate_issued_date is 'Дата выдачи СТС';
comment on column vehicle.transport.passport_number is 'Номер ПТС';
comment on column vehicle.transport.passport_issued_date is 'Дата выдачи ПТС';
comment on column vehicle.transport.brand_by_passport is 'Марка по ПТС';
comment on column vehicle.transport.model_by_passport is 'Модель по ПТС';
comment on column vehicle.transport.body_color is 'Цвет кузова';
comment on column vehicle.transport.telematics_id is 'ID телематики';
comment on column vehicle.transport.exploitation_start is 'Дата начала эксплуатации';
comment on column vehicle.transport.exploitation_end is 'Дата окончания эксплуатации';
comment on column vehicle.transport.current_mileage is 'Текущий пробег';