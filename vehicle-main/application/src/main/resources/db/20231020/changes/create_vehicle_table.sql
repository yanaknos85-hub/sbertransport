CREATE TABLE vehicle.vehicle
(
    id                           UUID         NOT NULL,
    model_id                     UUID         NOT NULL CONSTRAINT vehicle_vehicle_model_id_fk REFERENCES vehicle.model,
    category_id                  UUID         NOT NULL CONSTRAINT vehicle_vehicle_category_id_fk REFERENCES vehicle.category,
    manufacturer                 VARCHAR(255) NOT NULL,
    ecological_class             VARCHAR(1),
    engine_power                 SMALLINT     NOT NULL,
    engine_capacity              SMALLINT     NOT NULL,
    fuel_tank_volume             SMALLINT     NOT NULL,
    fuel_type_id                 UUID         NOT NULL CONSTRAINT vehicle_vehicle_fuel_type_id_fk REFERENCES vehicle.fuel_type,
    drive_id                     UUID         NOT NULL CONSTRAINT vehicle_vehicle_drive_id_fk REFERENCES vehicle.drive,
    mudguard_installed           BOOLEAN      NOT NULL,
    spare_wheel_holder_installed BOOLEAN      NOT NULL,
    weight                       INTEGER      NOT NULL,
    max_weight                   INTEGER      NOT NULL,
    height                       INTEGER      NOT NULL,
    width                        INTEGER      NOT NULL,
    length                       INTEGER      NOT NULL,
    service_interval_days        INTEGER      NOT NULL,
    service_interval_mileage     INTEGER      NOT NULL,
    service_authorization_days   INTEGER      NOT NULL,
    service_authorization_mileage   INTEGER      NOT NULL,
    CONSTRAINT pk_vehicle PRIMARY KEY (id)
);

CREATE UNIQUE INDEX vehicle_vehicle_valuable_fields_idx
    ON vehicle.vehicle (model_id, category_id, ecological_class, engine_power, engine_capacity, fuel_tank_volume,
                        fuel_type_id, drive_id, mudguard_installed, spare_wheel_holder_installed, weight, max_weight, height,
                        width, length);

comment on table vehicle.vehicle is 'Транспортное средство';
comment on column vehicle.vehicle.id is 'ID ТС';
comment on column vehicle.vehicle.model_id is 'ID Модель ТС';
comment on column vehicle.vehicle.category_id is 'ID Категория ТС';
comment on column vehicle.vehicle.manufacturer is 'Организация изготовитель (страна)';
comment on column vehicle.vehicle.ecological_class is 'Экологический класс';
comment on column vehicle.vehicle.engine_power is 'Мощность ЛС';
comment on column vehicle.vehicle.engine_capacity is 'Объем двигателя';
comment on column vehicle.vehicle.fuel_tank_volume is 'Объем топливного бака';
comment on column vehicle.vehicle.fuel_type_id is 'ID Вид топлива';
comment on column vehicle.vehicle.mudguard_installed is 'Наличие брызговиков';
comment on column vehicle.vehicle.spare_wheel_holder_installed is 'Держать запасного колеса';
comment on column vehicle.vehicle.weight is 'Масса без нагрузки';
comment on column vehicle.vehicle.max_weight is 'Макс снаряженная масса';
comment on column vehicle.vehicle.height is 'Высота, мм';
comment on column vehicle.vehicle.width is 'Ширина, мм';
comment on column vehicle.vehicle.length is 'Длина, мм';
comment on column vehicle.vehicle.service_interval_days is 'Межсервисный интервал по пробегу';
comment on column vehicle.vehicle.service_interval_mileage is 'Межсервисный интервал по времени';
comment on column vehicle.vehicle.service_authorization_days is 'Допуск по времени';
comment on column vehicle.vehicle.service_interval_mileage is 'Допуск по пробегу';