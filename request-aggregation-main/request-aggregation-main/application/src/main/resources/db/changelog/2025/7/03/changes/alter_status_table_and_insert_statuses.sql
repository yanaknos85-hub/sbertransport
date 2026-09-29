-- Добавление колонки status_description
ALTER TABLE request_aggregation.status
    ADD COLUMN status_description varchar(255) NOT NULL DEFAULT '';

-- Заполнение таблицы статусами
INSERT INTO request_aggregation.status (status_name, status_description)
VALUES ('PROCESSING', 'В обработке'),
       ('PROCESSED', 'Обработано'),
       ('GENERATING', 'Формируется'),
       ('GENERATED', 'Сформирован'),
       ('IN_REVIEW', 'На согласовании'),
       ('CREATED', 'Создана'),
       ('IN_PROGRESS', 'Поездка активна, в пути'),
       ('COMPLETED', 'Завершена'),
       ('CANCELED', 'Отменено на любой стадии пути');