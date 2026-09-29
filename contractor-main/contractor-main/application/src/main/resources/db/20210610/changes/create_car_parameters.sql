create table contractors.car_parameters (
    id              uuid not null,
    color           varchar(255) not null,
    mileage         float8 not null,
    year            int4 not null,
    car_model_id    uuid,
    trip_class_id   uuid,
    primary key (id)
);

alter table contractors.car_parameters add constraint car_parameters_to_model foreign key (car_model_id) references contractors.car_model;
alter table contractors.car_parameters add constraint parameters_to_trip_class foreign key (trip_class_id) references contractors.trip_classes;

COMMENT ON TABLE contractors.car_parameters IS 'Параметры автомобиля';
COMMENT ON COLUMN contractors.car_parameters.id IS 'Идентификатор';
COMMENT ON COLUMN contractors.car_parameters.color IS 'Цвет автомобиля';
COMMENT ON COLUMN contractors.car_parameters.mileage IS 'Допустимый пробег';
COMMENT ON COLUMN contractors.car_parameters.year IS 'Допустимый год выпуска';
COMMENT ON COLUMN contractors.car_parameters.car_model_id IS 'Модель';
COMMENT ON COLUMN contractors.car_parameters.trip_class_id IS 'Класс поездки';