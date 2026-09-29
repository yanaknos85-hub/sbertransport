-- Добавление колонки trip_description
ALTER TABLE request_aggregation.trip_type
    ADD COLUMN trip_description varchar(255) NOT NULL DEFAULT '';

-- Заполнение таблицы целями поездки
INSERT INTO request_aggregation.trip_type (trip_name, trip_description)
VALUES ('DAYTIME_TRIP', 'Поездка в дневное время'),
       ('HIGHTIME_EMPLOYEE_TRANSPORTATION', 'Доставка работников в ночное время');