-- Удаляем NOT NULL ограничения из таблицы point_lead
ALTER TABLE request_aggregation.point_lead ALTER COLUMN type_point DROP NOT NULL;
ALTER TABLE request_aggregation.point_lead ALTER COLUMN longitude DROP NOT NULL;
ALTER TABLE request_aggregation.point_lead ALTER COLUMN latitude DROP NOT NULL;
